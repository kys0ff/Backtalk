package off.kys.backtalk.presentation.state

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.util.emptyString

/**
 * Data class representing the state of the messages screen.
 *
 * @param messages The list of messages to display.
 * @param replyingTo The message being replied to, if any.
 * @param editingMessage The message being edited, if any.
 * @param selectedMessageIds The set of message IDs that are currently selected.
 */
data class MessagesUiState(
    val messages: List<MessageEntity> = emptyList(),
    val replyingTo: MessageEntity? = null,
    val editingMessage: MessageEntity? = null,
    val selectedMessageIds: Set<MessageId> = emptySet(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = emptyString(),
    val searchResults: List<MessageId> = emptyList(),
    val currentSearchResultIndex: Int = -1,
    val showPermissionRationale: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)