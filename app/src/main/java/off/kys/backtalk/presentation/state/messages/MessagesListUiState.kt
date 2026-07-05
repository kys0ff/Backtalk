package off.kys.backtalk.presentation.state.messages

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.util.emptyString

@Immutable
data class MessagesListUiState(
    val items: PersistentList<MessageItemUiState> = persistentListOf(),
    val selectionMode: Boolean = false,
    val searchQuery: String = emptyString(),
    val contextMenuEntityId: MessageId? = null,
    val hapticFeedbackEnabled: Boolean = true,
    val externalLinkWarningEnabled: Boolean = true,
    val disableContextMenuOnLongClick: Boolean = false
)