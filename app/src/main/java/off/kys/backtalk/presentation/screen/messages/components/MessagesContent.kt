package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.state.MessagesUiState

/**
 * Composable function that displays the messages content.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param state The current state of the messages screen.
 * @param onReply The callback function to handle replying to a message.
 * @param onSelect The callback function to handle selecting a message.
 * @param onSend The callback function to handle sending a message.
 */
@Composable
fun MessagesContent(
    modifier: Modifier,
    state: MessagesUiState,
    onReply: (MessageEntity?) -> Unit,
    onSelect: (MessageId?) -> Unit,
    onSend: (String) -> Unit
) {
    Column(modifier) {
        MessagesList(
            messages = state.messages,
            selectedMessageId = state.selectedMessageId,
            onReply = onReply,
            onSelect = onSelect
        )

        InputBar(
            replyingTo = state.replyingTo,
            onCancelReply = { onReply(null) },
            onMessageSend = onSend
        )
    }
}