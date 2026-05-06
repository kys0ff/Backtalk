package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.util.emptyString

/**
 * Composable function that displays the input bar for sending messages.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param messageInput The current input text for the message.
 * @param replyingTo The message being replied to, if any.
 * @param editingMessage The message being edited, if any.
 * @param onCancelReply The callback function to handle canceling the reply.
 * @param onCancelEdit The callback function to handle canceling the edit.
 * @param onMessageSend The callback function to handle sending a message.
 */
@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    messageInput: String,
    replyingTo: MessageEntity?,
    editingMessage: MessageEntity?,
    onCancelReply: () -> Unit,
    onCancelEdit: () -> Unit,
    onMessageSend: (String) -> Unit
) {
    var textValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = messageInput,
                selection = TextRange(messageInput.length)
            )
        )
    }

    LaunchedEffect(key1 = messageInput) {
        if (messageInput != textValue.text) {
            textValue =
                TextFieldValue(text = messageInput, selection = TextRange(messageInput.length))
        }
    }

    fun applyStyle(startSym: String, endSym: String) {
        val selection = textValue.selection
        val text = textValue.text
        val selectedText = text.substring(selection.start, selection.end)
        val newText =
            text.replaceRange(selection.start, selection.end, "$startSym$selectedText$endSym")
        val newCursorPos = selection.start + startSym.length + selectedText.length + endSym.length
        textValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPos)
        )
    }

    Surface(
        modifier = Modifier.imePadding(),
        tonalElevation = 2.dp
    ) {
        Column(modifier = modifier) {
            AnimatedVisibility(
                visible = replyingTo != null || editingMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(if (editingMessage != null) R.string.common_edit else R.string.chat_replying_to),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SmartText(
                            text = (editingMessage ?: replyingTo)?.text ?: emptyString(),
                            clickableLink = false,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(
                        onClick = if (editingMessage != null) onCancelEdit else onCancelReply,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_close_24),
                            contentDescription = stringResource(R.string.common_cancel)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    textStyle = TextStyle(textDirection = TextDirection.Content),
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                )

                IconButton(
                    onClick = {
                        if (textValue.text.isNotBlank()) {
                            onMessageSend(textValue.text)
                            textValue = TextFieldValue(emptyString())
                        }
                    },
                    enabled = textValue.text.isNotBlank()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_send_24),
                        contentDescription = stringResource(R.string.common_send),
                        tint = if (textValue.text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            AnimatedVisibility(
                visible = textValue.selection.length > 0,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormattingButton(label = "B") { applyStyle("**", "**") }
                    FormattingButton(label = "I") { applyStyle("*", "*") }
                    FormattingButton(label = "U") { applyStyle("__", "__") }
                    FormattingButton(label = "S") { applyStyle("~~", "~~") }
                    FormattingButton(label = "M") { applyStyle("`", "`") }
                }
            }
        }
    }
}

@Composable
private fun FormattingButton(
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
