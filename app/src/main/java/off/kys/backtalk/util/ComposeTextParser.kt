package off.kys.backtalk.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

/**
 * A utility object responsible for parsing Markdown-like syntax into a Jetpack Compose [AnnotatedString].
 * Now upgraded to support line-level block elements like lists and task checkboxes.
 */
object ComposeTextParser {

    private data class StyleDef(
        val delimiter: String,
        val style: SpanStyle
    )

    private val STYLES = listOf(
        StyleDef("**", SpanStyle(fontWeight = FontWeight.Bold)),
        StyleDef("__", SpanStyle(textDecoration = TextDecoration.Underline)),
        StyleDef("~~", SpanStyle(textDecoration = TextDecoration.LineThrough)),
        StyleDef("*", SpanStyle(fontStyle = FontStyle.Italic)),
        StyleDef("`", SpanStyle(fontFamily = FontFamily.Monospace))
    )

    val MARKDOWN_LINK_REGEX = Regex("""\[([^]]+)]\(([^)]+)\)""")
    val NAKED_URL_REGEX = Regex("""(https?://[^\s)\]]+)""")
    val MENTION_REGEX = Regex("""@(\w+)""")
    val HASHTAG_REGEX = Regex("""#(\w+)""")

    // Block level Regex matchers for lines
    private val CHECKBOX_UNCHECKED_REGEX = Regex("""^(\s*)[-*+]\s+\[\s]\s+(.*)""")
    private val CHECKBOX_CHECKED_REGEX = Regex("""^(\s*)[-*+]\s+\[[xX]]\s+(.*)""")
    private val UNORDERED_LIST_REGEX = Regex("""^(\s*)[-*+]\s+(.*)""")
    private val ORDERED_LIST_REGEX = Regex("""^(\s*)(\d+\.)\s+(.*)""")

    fun toAnnotatedString(
        text: String,
        linkStyles: TextLinkStyles? = null,
        onAnnotationClicked: ((LinkAnnotation) -> Unit)? = null
    ): AnnotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            parseLine(line, this, linkStyles, onAnnotationClicked)
            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }

    private fun parseLine(
        line: String,
        builder: AnnotatedString.Builder,
        linkStyles: TextLinkStyles?,
        onAnnotationClicked: ((LinkAnnotation) -> Unit)?
    ) {
        // 1. Check for Unchecked Checkbox (- [ ])
        val uncheckedMatch = CHECKBOX_UNCHECKED_REGEX.matchEntire(line)
        if (uncheckedMatch != null) {
            val indent = uncheckedMatch.groupValues[1]
            val content = uncheckedMatch.groupValues[2]
            builder.append("$indent☐  ")
            parseRecursive(content, builder, linkStyles, onAnnotationClicked)
            return
        }

        // 2. Check for Checked Checkbox (- [x])
        val checkedMatch = CHECKBOX_CHECKED_REGEX.matchEntire(line)
        if (checkedMatch != null) {
            val indent = checkedMatch.groupValues[1]
            val content = checkedMatch.groupValues[2]
            builder.append("$indent☑  ")
            // Optional flavor: Wrap checked text content in a strikethrough style
            builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                parseRecursive(content, this, linkStyles, onAnnotationClicked)
            }
            return
        }

        // 3. Check for Unordered List Item (- or *)
        val unorderedMatch = UNORDERED_LIST_REGEX.matchEntire(line)
        if (unorderedMatch != null) {
            val indent = unorderedMatch.groupValues[1]
            val content = unorderedMatch.groupValues[2]
            builder.append("$indent•  ")
            parseRecursive(content, builder, linkStyles, onAnnotationClicked)
            return
        }

        // 4. Check for Ordered List Item (1.)
        val orderedMatch = ORDERED_LIST_REGEX.matchEntire(line)
        if (orderedMatch != null) {
            val indent = orderedMatch.groupValues[1]
            val numberToken = orderedMatch.groupValues[2]
            val content = orderedMatch.groupValues[3]
            builder.append("$indent$numberToken  ")
            parseRecursive(content, builder, linkStyles, onAnnotationClicked)
            return
        }

        // Plain line fallback
        parseRecursive(line, builder, linkStyles, onAnnotationClicked)
    }

    private fun parseRecursive(
        text: String,
        builder: AnnotatedString.Builder,
        linkStyles: TextLinkStyles? = null,
        onAnnotationClicked: ((LinkAnnotation) -> Unit)? = null
    ) {
        if (text.isEmpty()) return

        var earliestMatch = -1
        var bestStyle: StyleDef? = null
        var bestClosingIndex = -1
        var linkMatch: MatchResult? = null
        var isNakedUrl = false
        var mentionMatch: MatchResult? = null

        // Check for basic styles
        for (i in text.indices) {
            for (styleDef in STYLES) {
                if (text.startsWith(styleDef.delimiter, i)) {
                    val closingIndex = findClosingTag(text, i + styleDef.delimiter.length, styleDef.delimiter)
                    if (closingIndex != -1) {
                        earliestMatch = i
                        bestStyle = styleDef
                        bestClosingIndex = closingIndex
                        break
                    }
                }
            }
            if (earliestMatch != -1) break
        }

        // Check for Markdown links
        val mLink = MARKDOWN_LINK_REGEX.find(text)
        if (mLink != null && (earliestMatch == -1 || mLink.range.first < earliestMatch)) {
            earliestMatch = mLink.range.first
            bestStyle = null
            linkMatch = mLink
            isNakedUrl = false
            mentionMatch = null
        }

        // Check for naked URLs
        val nLink = NAKED_URL_REGEX.find(text)
        if (nLink != null && (earliestMatch == -1 || nLink.range.first < earliestMatch)) {
            earliestMatch = nLink.range.first
            bestStyle = null
            linkMatch = nLink
            isNakedUrl = true
            mentionMatch = null
        }

        // Check for mentions
        val mention = MENTION_REGEX.find(text)
        if (mention != null && (earliestMatch == -1 || mention.range.first < earliestMatch)) {
            earliestMatch = mention.range.first
            bestStyle = null
            linkMatch = null
            mentionMatch = mention
        }

        // Check for hashtags
        var hashtagMatch: MatchResult? = null
        val hashtag = HASHTAG_REGEX.find(text)
        if (hashtag != null && (earliestMatch == -1 || hashtag.range.first < earliestMatch)) {
            earliestMatch = hashtag.range.first
            bestStyle = null
            linkMatch = null
            mentionMatch = null
            hashtagMatch = hashtag
        }

        if (earliestMatch == -1) {
            builder.append(text)
            return
        }

        builder.append(text.substring(0, earliestMatch))

        when {
            bestStyle != null -> {
                val delimiter = bestStyle.delimiter
                builder.withStyle(bestStyle.style) {
                    parseRecursive(
                        text.substring(earliestMatch + delimiter.length, bestClosingIndex),
                        this, linkStyles, onAnnotationClicked
                    )
                }
                parseRecursive(
                    text.substring(bestClosingIndex + delimiter.length),
                    builder,
                    linkStyles,
                    onAnnotationClicked
                )
            }

            linkMatch != null -> {
                val url = if (isNakedUrl) linkMatch.value else linkMatch.groupValues[2]
                val displayText = if (isNakedUrl) linkMatch.value else linkMatch.groupValues[1]

                builder.withLink(
                    LinkAnnotation.Url(
                        url = url,
                        styles = linkStyles,
                        linkInteractionListener = onAnnotationClicked
                    )
                ) {
                    append(displayText)
                }
                parseRecursive(
                    text.substring(linkMatch.range.last + 1),
                    builder,
                    linkStyles,
                    onAnnotationClicked
                )
            }

            mentionMatch != null -> {
                val username = mentionMatch.groupValues[1]
                builder.withLink(
                    LinkAnnotation.Clickable(
                        tag = username,
                        styles = linkStyles,
                        linkInteractionListener = onAnnotationClicked
                    )
                ) {
                    append("@$username")
                }
                parseRecursive(
                    text.substring(mentionMatch.range.last + 1),
                    builder,
                    linkStyles,
                    onAnnotationClicked
                )
            }

            hashtagMatch != null -> {
                val tag = hashtagMatch.groupValues[1]
                builder.withLink(
                    LinkAnnotation.Clickable(
                        tag = "hashtag:$tag",
                        styles = linkStyles,
                        linkInteractionListener = onAnnotationClicked
                    )
                ) {
                    append("#$tag")
                }
                parseRecursive(
                    text.substring(hashtagMatch.range.last + 1),
                    builder,
                    linkStyles,
                    onAnnotationClicked
                )
            }
        }
    }

    private fun findClosingTag(text: String, startIndex: Int, delimiter: String): Int {
        var i = startIndex
        while (i <= text.length - delimiter.length) {
            if (text.startsWith(delimiter, i)) {
                val largerDelimiter = STYLES.find {
                    it.delimiter != delimiter &&
                            it.delimiter.startsWith(delimiter) &&
                            text.startsWith(it.delimiter, i)
                }

                if (largerDelimiter != null) {
                    i += largerDelimiter.delimiter.length
                    continue
                }
                return i
            }
            i++
        }
        return -1
    }
}