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
 * A Markdown-ish parser that doesn't give up when things get complicated.
 */
object MarkdownParser {

    private data class StyleDef(
        val delimiter: String,
        val style: SpanStyle
    )

    // Order matters: longer delimiters must come before shorter ones sharing the same prefix.
    private val STYLES = listOf(
        StyleDef("**", SpanStyle(fontWeight = FontWeight.Bold)),
        StyleDef("__", SpanStyle(textDecoration = TextDecoration.Underline)),
        StyleDef("~~", SpanStyle(textDecoration = TextDecoration.LineThrough)),
        StyleDef("*", SpanStyle(fontStyle = FontStyle.Italic)),
        StyleDef("`", SpanStyle(fontFamily = FontFamily.Monospace))
    )

    private val MARKDOWN_LINK_REGEX = Regex("""\[([^\]]+)\]\(([^)]+)\)""")
    private val NAKED_URL_REGEX = Regex("""(https?://[^\s)\]]+)""")

    fun toAnnotatedString(
        text: String,
        linkStyles: TextLinkStyles? = null,
        onLinkClicked: ((LinkAnnotation) -> Unit)? = null
    ): AnnotatedString = buildAnnotatedString {
        parseRecursive(text, this, linkStyles, onLinkClicked)
    }

    private fun parseRecursive(
        text: String,
        builder: AnnotatedString.Builder,
        linkStyles: TextLinkStyles? = null,
        onLinkClicked: ((LinkAnnotation) -> Unit)? = null
    ) {
        if (text.isEmpty()) return

        var earliestMatch = -1
        var bestStyle: StyleDef? = null
        var bestClosingIndex = -1
        var linkMatch: MatchResult? = null
        var isNakedUrl = false

        // 1. Check for Style tags
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

        // 2. Check for Markdown links
        val mLink = MARKDOWN_LINK_REGEX.find(text)
        if (mLink != null && (earliestMatch == -1 || mLink.range.first < earliestMatch)) {
            earliestMatch = mLink.range.first
            bestStyle = null
            linkMatch = mLink
            isNakedUrl = false
        }

        // 3. Check for Naked URLs
        val nLink = NAKED_URL_REGEX.find(text)
        if (nLink != null && (earliestMatch == -1 || nLink.range.first < earliestMatch)) {
            earliestMatch = nLink.range.first
            bestStyle = null
            linkMatch = nLink
            isNakedUrl = true
        }

        if (earliestMatch == -1) {
            builder.append(text)
            return
        }

        // Append text before the match
        builder.append(text.substring(0, earliestMatch))

        if (bestStyle != null) {
            val delimiter = bestStyle.delimiter
            builder.withStyle(bestStyle.style) {
                parseRecursive(text.substring(earliestMatch + delimiter.length, bestClosingIndex), this, linkStyles, onLinkClicked)
            }
            parseRecursive(text.substring(bestClosingIndex + delimiter.length), builder, linkStyles, onLinkClicked)
        } else if (linkMatch != null) {
            val url = if (isNakedUrl) linkMatch.value else linkMatch.groupValues[2]
            val displayText = if (isNakedUrl) linkMatch.value else linkMatch.groupValues[1]

            builder.withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = linkStyles,
                    linkInteractionListener = onLinkClicked
                )
            ) {
                append(displayText)
            }
            parseRecursive(text.substring(linkMatch.range.last + 1), builder, linkStyles, onLinkClicked)
        }
    }

    private fun findClosingTag(text: String, startIndex: Int, delimiter: String): Int {
        var i = startIndex
        while (i <= text.length - delimiter.length) {
            if (text.startsWith(delimiter, i)) {
                // Prevent shorter tags from accidentally matching inside longer tags (e.g., "*" matching inside "**")
                val largerDelimiter = STYLES.find {
                    it.delimiter != delimiter &&
                            it.delimiter.startsWith(delimiter) &&
                            text.startsWith(it.delimiter, i)
                }

                if (largerDelimiter != null) {
                    // Skip past the larger delimiter entirely so we don't accidentally match inside it
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