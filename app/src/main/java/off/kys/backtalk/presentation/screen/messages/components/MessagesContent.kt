package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.viewmodel.MessagesUiState

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