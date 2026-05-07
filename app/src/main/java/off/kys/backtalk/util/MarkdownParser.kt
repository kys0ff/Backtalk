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
 * A Markdown-ish parser that handles styles, links, and mentions.
 */
object MarkdownParser {

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

    private val MARKDOWN_LINK_REGEX = Regex("""\[([^]]+)]\(([^)]+)\)""")
    private val NAKED_URL_REGEX = Regex("""(https?://[^\s)\]]+)""")
    private val MENTION_REGEX = Regex("""@(\w+)""")

    fun toAnnotatedString(
        text: String,
        linkStyles: TextLinkStyles? = null,
        highlightQuery: String? = null,
        onAnnotationClicked: ((LinkAnnotation) -> Unit)? = null
    ): AnnotatedString = buildAnnotatedString {
        val base = buildAnnotatedString {
            parseRecursive(text, this, linkStyles, onAnnotationClicked)
        }
        append(base)

        if (!highlightQuery.isNullOrBlank()) {
            val terms = highlightQuery.lowercase().split(" ").filter { it.isNotBlank() }
            val lowerText = base.text.lowercase()
            for (term in terms) {
                var index = lowerText.indexOf(term)
                while (index != -1) {
                    addStyle(
                        style = SpanStyle(background = androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.4f)),
                        start = index,
                        end = index + term.length
                    )
                    index = lowerText.indexOf(term, index + term.length)
                }
            }
        }
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

        if (earliestMatch == -1) {
            builder.append(text)
            return
        }

        builder.append(text.substring(0, earliestMatch))

        when {
            bestStyle != null -> {
                val delimiter = bestStyle.delimiter
                builder.withStyle(bestStyle.style) {
                    parseRecursive(text.substring(earliestMatch + delimiter.length, bestClosingIndex), this, linkStyles, onAnnotationClicked)
                }
                parseRecursive(text.substring(bestClosingIndex + delimiter.length), builder, linkStyles, onAnnotationClicked)
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
                parseRecursive(text.substring(linkMatch.range.last + 1), builder, linkStyles, onAnnotationClicked)
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
                parseRecursive(text.substring(mentionMatch.range.last + 1), builder, linkStyles, onAnnotationClicked)
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