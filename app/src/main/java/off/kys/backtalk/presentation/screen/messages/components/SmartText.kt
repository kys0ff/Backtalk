package off.kys.backtalk.presentation.screen.messages.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.util.MarkdownParser
import off.kys.backtalk.util.emptyString

/**
 * A custom [Text] composable that handles Markdown, links, and mentions.
 */
@Composable
fun SmartText(
    text: String,
    modifier: Modifier = Modifier,
    clickableLink: Boolean = true,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = MaterialTheme.colorScheme.background,
    style: TextStyle = LocalTextStyle.current,
    textDecoration: TextDecoration = TextDecoration.None,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    highlightQuery: String? = null,
    onMentionClicked: (String) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val showSafetyDialog = remember { mutableStateOf(false) }
    var pendingUrl by remember { mutableStateOf(emptyString()) }

    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.inversePrimary,
            textDecoration = TextDecoration.Underline
        )
    )

    val annotatedString = MarkdownParser.toAnnotatedString(
        text = text,
        linkStyles = linkStyles,
        highlightQuery = highlightQuery,
        onAnnotationClicked = { annotation ->
            when (annotation) {
                is LinkAnnotation.Url -> {
                    if (clickableLink) {
                        pendingUrl = annotation.url
                        showSafetyDialog.value = true
                    }
                }

                is LinkAnnotation.Clickable -> {
                    onMentionClicked(annotation.tag)
                }
            }
        }
    )

    Text(
        text = annotatedString,
        modifier = modifier,
        maxLines = maxLines,
        lineHeight = lineHeight,
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