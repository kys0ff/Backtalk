package off.kys.backtalk.presentation.state

import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.components.status_scaffold.ScaffoldStatus
import off.kys.backtalk.presentation.components.status_scaffold.StatusMessage
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
    val showDeleteConfirmation: Boolean = false,
    val isLoading: Boolean = true,
    val selectedTag: String? = null,
    val pinnedMessages: List<MessageEntity> = emptyList(),
    val activePinnedMessageIndex: Int = 0,
    val shouldScrollToPinned: Boolean = false,
    val scrollToSearchTrigger: Long = 0L,
    val showPinnedMessagesDialog: Boolean = false,
    val blinkMessageId: MessageId? = null,
    val filteredMessages: List<MessageEntity> = emptyList(),
    val showMediaPicker: Boolean = false,
    val showSharedMediaSheet: Boolean = false,
    val shouldScrollToBottom: Boolean = false,
    val selectedImagePaths: Map<MessageId, Set<String>> = emptyMap(),
    val showChangelogDialog: Boolean = false,
    val showTagsBar: Boolean = true,
    val sharedText: String? = null,
    val sharedImageUris: List<String> = emptyList(),
    val scaffoldStatus: ScaffoldStatus = ScaffoldStatus.None,
    val scaffoldMessage: StatusMessage? = null,
    val messageContextMenuEntity: MessageEntity? = null
) {
    val selectionMetrics: SelectionMetrics
        get() {
            val selectedMessagesCount = selectedMessageIds.size
            val selectedImagesCount = selectedImagePaths.values.sumOf { it.size }

            val totalSelectedCount = selectedMessagesCount + selectedImagePaths.filterKeys {
                it !in selectedMessageIds
            }.values.sumOf { it.size }

            val currentTime = System.currentTimeMillis()
            val deletableMessagesCount = messages.count {
                it.id in selectedMessageIds && (currentTime - it.timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW
            }

            val deletableImagesCount = selectedImagePaths.filterKeys { it !in selectedMessageIds }.entries.sumOf { (messageId, paths) ->
                val message = messages.find { it.id == messageId }
                if (message != null && (currentTime - message.timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW) {
                    paths.size
                } else 0
            }

            return SelectionMetrics(
                selectedMessagesCount = selectedMessagesCount,
                selectedImagesCount = selectedImagesCount,
                totalSelectedCount = totalSelectedCount,
                totalDeletableCount = deletableMessagesCount + deletableImagesCount
            )
        }
}
