package off.kys.backtalk.presentation.state.messages

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.components.status_scaffold.ScaffoldStatus
import off.kys.backtalk.presentation.components.status_scaffold.StatusMessage
import off.kys.backtalk.presentation.model.MessageUiModel
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
    val messages: PersistentList<MessageUiModel> = persistentListOf(),
    val replyingTo: MessageUiModel? = null,
    val editingMessage: MessageUiModel? = null,
    val selectedMessageIds: PersistentSet<MessageId> = persistentSetOf(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = emptyString(),
    val searchResults: PersistentList<MessageId> = persistentListOf(),
    val currentSearchResultIndex: Int = -1,
    val showPermissionRationale: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isLoading: Boolean = true,
    val selectedTag: String? = null,
    val pinnedMessages: PersistentList<MessageUiModel> = persistentListOf(),
    val activePinnedMessageIndex: Int = 0,
    val shouldScrollToPinned: Boolean = false,
    val scrollToSearchTrigger: Long = 0L,
    val showPinnedMessagesDialog: Boolean = false,
    val blinkMessageId: MessageId? = null,
    val filteredMessages: PersistentList<MessageUiModel> = persistentListOf(),
    val showMediaPicker: Boolean = false,
    val showSharedMediaSheet: Boolean = false,
    val shouldScrollToBottom: Boolean = false,
    val selectedImagePaths: PersistentMap<MessageId, PersistentSet<String>> = persistentMapOf(),
    val showChangelogDialog: Boolean = false,
    val showTagsBar: Boolean = true,
    val sharedText: String? = null,
    val sharedImageUris: PersistentList<String> = persistentListOf(),
    val scaffoldStatus: ScaffoldStatus = ScaffoldStatus.None,
    val scaffoldMessage: StatusMessage? = null,
    val messageContextMenuEntity: MessageUiModel? = null,
    val selectionMetrics: SelectionMetrics = SelectionMetrics(),
    val hashtags: PersistentList<String> = persistentListOf(),
    val repliedMessagesMap: PersistentMap<MessageId, MessageUiModel> = persistentMapOf(),
    val hapticFeedbackEnabled: Boolean = true,
    val swipeHintShown: Boolean = true,
    val externalLinkWarningEnabled: Boolean = true,
    val disableContextMenuOnLongClick: Boolean = false
)