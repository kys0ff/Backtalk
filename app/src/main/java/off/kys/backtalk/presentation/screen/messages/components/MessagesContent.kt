package off.kys.backtalk.presentation.screen.messages.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.state.MessagesUiState
import off.kys.backtalk.util.emptyString

/**
 * Composable function that displays the messages content.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param state The current state of the messages screen.
 * @param onEditMessage The callback function to handle editing a message.
 * @param onReply The callback function to handle replying to a message.
 * @param onToggleSelect The callback function to toggle the selection state of a message.
 * @param onSend The callback function to handle sending a message.
 */
@Composable
fun MessagesContent(
    modifier: Modifier,
    state: MessagesUiState,
    listState: LazyListState,
    onEditMessage: (MessageEntity?) -> Unit,
    onReply: (MessageEntity?) -> Unit,
    onToggleSelect: (MessageId) -> Unit,
    onSend: (String) -> Unit,
    onSendVoice: (String, Long, List<Float>) -> Unit,
    onSchedule: (String, Long) -> Unit,
    onDismissRationale: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        if (state.showPermissionRationale) {
            PermissionRationaleDialog(
                onDismiss = onDismissRationale,
                onConfirm = {
                    onDismissRationale()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        if (state.showDeleteConfirmation) {
            DeleteConfirmationDialog(
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }

        MessagesList(
            messages = state.messages,
            selectedMessageIds = state.selectedMessageIds,
            listState = listState,
            onEditMessage = onEditMessage,
            onReply = onReply,
            onToggleSelect = onToggleSelect,
            searchQuery = if (state.isSearchActive) state.searchQuery else emptyString()
        )

        InputBar(
            messageInput = state.editingMessage?.let { it.editedText ?: it.text }.orEmpty(),
            replyingTo = state.replyingTo,
            editingMessage = state.editingMessage,
            onCancelReply = { onReply(null) },
            onCancelEdit = { onEditMessage(null) },
            onMessageSend = onSend,
            onVoiceSend = onSendVoice,
            onMessageSchedule = onSchedule
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.chat_delete_selected_title)) },
        text = { Text(text = stringResource(R.string.chat_delete_selected_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    )
}
