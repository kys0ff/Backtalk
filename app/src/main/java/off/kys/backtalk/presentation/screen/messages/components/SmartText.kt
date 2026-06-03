package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.util.ComposeTextParser
import off.kys.backtalk.util.emptyString
import org.koin.compose.koinInject
import kotlin.math.abs

/**
 * A highly customizable text component that supports Markdown formatting, interactive links,
 * user mentions, and search result highlighting.
 *
 * It automatically parses the provided [text] using [ComposeTextParser], applies custom styles
 * for links, and handles click events. It also features an optional safety dialog for
 * external URLs based on user preferences.
 *
 * @param text The raw string content to be parsed and displayed.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param clickableLink Whether URLs within the text should be interactive.
 * @param fontSize The size of the glyphs to use when painting the text.
 * @param color The color to be applied to the text.
 * @param style Style configuration for the text such as color, font, line height etc.
 * @param textDecoration The decorations to paint on the text (e.g., underline).
 */
@Composable
fun SmartText(
    text: String,
    modifier: Modifier = Modifier,
    clickableLink: Boolean = true,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle = LocalTextStyle.current,
    textDecoration: TextDecoration = TextDecoration.None,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    highlightQuery: String? = null,
    overflow: TextOverflow = TextOverflow.Visible,
    onMentionClicked: (String) -> Unit = {}
) {
    val preferences = koinInject<BacktalkPreferences>()
    SmartTextContent(
        text = text,
        modifier = modifier,
        clickableLink = clickableLink,
        fontSize = fontSize,
        color = color,
        style = style,
        textDecoration = textDecoration,
        maxLines = maxLines,
        lineHeight = lineHeight,
        highlightQuery = highlightQuery,
        overflow = overflow,
        onMentionClicked = onMentionClicked,
        externalLinkWarningEnabled = preferences.externalLinkWarningEnabled
    )
}

@Composable
fun SmartTextContent(
    text: String,
    modifier: Modifier = Modifier,
    clickableLink: Boolean = true,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle = LocalTextStyle.current,
    textDecoration: TextDecoration = TextDecoration.None,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    highlightQuery: String? = null,
    overflow: TextOverflow = TextOverflow.Visible,
    onMentionClicked: (String) -> Unit = {},
    externalLinkWarningEnabled: Boolean
) {
    val uriHandler = LocalUriHandler.current
    val showSafetyDialog = remember { mutableStateOf(false) }
    var pendingUrl by remember { mutableStateOf(emptyString()) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var pressedLink by remember { mutableStateOf<AnnotatedString.Range<LinkAnnotation>?>(null) }
    val highlightColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    val rippleColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)

    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.inversePrimary,
            textDecoration = TextDecoration.Underline
        )
    )

    val annotatedString = ComposeTextParser.toAnnotatedString(
        text = text,
        linkStyles = linkStyles,
        onAnnotationClicked = { annotation ->
            when (annotation) {
                is LinkAnnotation.Url -> {
                    if (clickableLink) {
                        if (externalLinkWarningEnabled) {
                            pendingUrl = annotation.url
                            showSafetyDialog.value = true
                        } else {
                            uriHandler.openUri(annotation.url)
                        }
                    }
                }

                is LinkAnnotation.Clickable -> {
                    if (annotation.tag.startsWith("hashtag:")) {
                        val tag = annotation.tag.removePrefix("hashtag:")
                        onMentionClicked(tag) // Reusing onMentionClicked for hashtags for now
                    } else {
                        onMentionClicked(annotation.tag)
                    }
                }
            }
        }
    )

    Text(
        text = annotatedString,
        onTextLayout = { textLayoutResult = it },
        modifier = modifier
            .pointerInput(annotatedString) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val layout = textLayoutResult ?: return@awaitEachGesture
                    val offset = layout.getOffsetForPosition(down.position)
                    val link = annotatedString.getLinkAnnotations(offset, offset).firstOrNull()
                    if (link != null) {
                        pressedLink = link
                        waitForUpOrCancellation()
                        pressedLink = null
                    }
                }
            }
            .drawBehind {
                val layout = textLayoutResult ?: return@drawBehind

                fun drawHighlight(start: Int, end: Int, color: Color) {
                    if (end <= layout.layoutInput.text.length) {
                        val startLine = layout.getLineForOffset(start)
                        val endLine = layout.getLineForOffset(end)

                        for (line in startLine..endLine) {
                            val lineStartOffset = layout.getLineStart(line)
                            val isLtr =
                                layout.getParagraphDirection(lineStartOffset) == ResolvedTextDirection.Ltr

                            val left = if (line == startLine) {
                                layout.getHorizontalPosition(start, usePrimaryDirection = true)
                            } else {
                                if (isLtr) layout.getLineLeft(line) else layout.getLineRight(line)
                            }

                            val right = if (line == endLine) {
                                layout.getHorizontalPosition(end, usePrimaryDirection = true)
                            } else {
                                if (isLtr) layout.getLineRight(line) else layout.getLineLeft(line)
                            }

                            val top = layout.getLineTop(line)
                            val bottom = layout.getLineBottom(line)

                            drawRoundRect(
                                color = color,
                                topLeft = Offset(minOf(left, right), top),
                                size = Size(abs(right - left), bottom - top),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }

                // Draw pressed link ripple
                pressedLink?.let {
                    drawHighlight(
                        it.start,
                        it.end,
                        rippleColor
                    )
                }

                // Draw search highlights
                if (!highlightQuery.isNullOrBlank()) {
                    val terms = highlightQuery.lowercase().split(" ").filter { it.isNotBlank() }
                    val lowerText = annotatedString.text.lowercase()

                    for (term in terms) {
                        var index = lowerText.indexOf(term)
                        while (index != -1) {
                            drawHighlight(index, index + term.length, highlightColor)
                            index = lowerText.indexOf(term, index + term.length)
                        }
                    }
                }
            },
        maxLines = maxLines,
        lineHeight = lineHeight,
        overflow = overflow,
        style = style.copy(
            textDirection = TextDirection.Content,
            color = if (color != Color.Unspecified) color else style.color,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            textDecoration = textDecoration,
            textAlign = TextAlign.Start,
        )
    )

    if (showSafetyDialog.value) {
        LinkSafetyDialog(
            url = pendingUrl,
            onConfirm = {
                uriHandler.openUri(pendingUrl)
                showSafetyDialog.value = false
            },
            onDismiss = {
                showSafetyDialog.value = false
            }
        )
    }
}

@Composable
private fun LinkSafetyDialog(
    url: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_warning_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = stringResource(R.string.link_safety_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.link_safety_message),
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = url,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.link_safety_open))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.link_safety_cancel))
            }
        }
    )
}