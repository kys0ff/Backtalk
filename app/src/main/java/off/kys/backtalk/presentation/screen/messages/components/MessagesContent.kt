package off.kys.backtalk.presentation.screen.messages.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.model.MessageUiModel
import off.kys.backtalk.presentation.screen.messages.LocalMessagesActions
import off.kys.backtalk.presentation.state.messages.MessagesUiState
import off.kys.backtalk.presentation.state.messages.MessagesListActions
import off.kys.backtalk.presentation.state.messages.MessagesListUiState
import off.kys.backtalk.util.emptyString

/**
 * Composable function that displays the primary content of the messages screen, including
 * the list of messages and relevant dialogs for permissions and deletions.
 */
@Composable
fun MessagesContent(
    modifier: Modifier,
    state: MessagesUiState,
    tags: List<String>,
    listState: LazyListState,
    totalDeletableCount: Int = 0,
    totalSelectedCount: Int = 0,
    bottomPadding: Dp = 0.dp
) {
    val actions = LocalMessagesActions.current
    val context = LocalContext.current
    val isSelectionMode = totalSelectedCount > 0

    val pinnedVisible = state.pinnedMessages.isNotEmpty() && !isSelectionMode
    val tagsVisible = tags.isNotEmpty() && !isSelectionMode && state.showTagsBar

    val topPadding = when {
        pinnedVisible && tagsVisible -> 116.dp
        pinnedVisible -> 68.dp
        tagsVisible -> 52.dp
        else -> 0.dp
    }

    val listActions = remember(actions) {
        object : MessagesListActions {
            override fun onReply(message: MessageUiModel) =
                actions.onEvent(MessagesUiEvent.ReplyTo(message))

            override fun onEdit(message: MessageUiModel) =
                actions.onEvent(MessagesUiEvent.EditMessage(message))

            override fun onDelete(message: MessageUiModel) =
                actions.onEvent(MessagesUiEvent.DeleteMessage(message))

            override fun onCopy(message: MessageUiModel) =
                actions.onEvent(MessagesUiEvent.CopyMessage(message))

            override fun onTogglePin(message: MessageUiModel) =
                actions.onEvent(MessagesUiEvent.TogglePinMessage(message.id, !message.isPinned))

            override fun onToggleSelect(messageId: MessageId) =
                actions.onEvent(MessagesUiEvent.ToggleSelection(messageId))

            override fun onLongClick(message: MessageUiModel?) =
                actions.onEvent(MessagesUiEvent.ShowMessageContextMenu(message))

            override fun onScrollToMessage(messageId: MessageId) =
                actions.onScrollToMessage(messageId)

            override fun onTagClick(tag: String) = actions.onEvent(MessagesUiEvent.SelectTag(tag))
            override fun onToggleImageSelect(messageId: MessageId, path: String) =
                actions.onEvent(MessagesUiEvent.ToggleImageSelection(messageId, path))

            override fun onMarkSwipeHintShown() =
                actions.onEvent(MessagesUiEvent.MarkSwipeHintShown)
        }
    }

    val listUiState = remember(state) {
        val showHintForId = if (!state.swipeHintShown) {
            state.filteredMessages.lastOrNull { it.canEdit }?.id
        } else null

        val items = MessageItemUiMapper.map(
            messages = state.filteredMessages,
            repliedMessagesMap = state.repliedMessagesMap,
            selectedMessageIds = state.selectedMessageIds,
            selectedImagePaths = state.selectedImagePaths,
            blinkMessageId = state.blinkMessageId,
            showHintForId = showHintForId
        )

        MessagesListUiState(
            items = items,
            selectionMode = state.selectedMessageIds.isNotEmpty() || state.selectedImagePaths.isNotEmpty(),
            searchQuery = if (state.isSearchActive) state.searchQuery else emptyString(),
            contextMenuEntityId = state.messageContextMenuEntity?.id,
            hapticFeedbackEnabled = state.hapticFeedbackEnabled,
            externalLinkWarningEnabled = state.externalLinkWarningEnabled,
            disableContextMenuOnLongClick = state.disableContextMenuOnLongClick
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        MessagesList(
            state = listUiState,
            actions = listActions,
            listState = listState,
            contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding)
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = pinnedVisible,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                PinnedMessageBar(
                    pinnedMessages = state.pinnedMessages,
                    activeIndex = state.activePinnedMessageIndex,
                    onClick = { actions.onNavigatePinned() },
                    onListClick = { actions.onEvent(MessagesUiEvent.TogglePinnedMessagesDialog(true)) }
                )
            }

            AnimatedVisibility(
                visible = tagsVisible,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                TagFilterBar(
                    tags = tags,
                    selectedTag = state.selectedTag,
                    onTagClick = { actions.onEvent(MessagesUiEvent.SelectTag(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.showPinnedMessagesDialog) {
            PinnedMessagesDialog(
                pinnedMessages = state.pinnedMessages,
                onMessageClick = { actions.onScrollToMessage(it.id) },
                onUnpinClick = { actions.onEvent(MessagesUiEvent.TogglePinMessage(it.id, false)) },
                onDismiss = { actions.onEvent(MessagesUiEvent.TogglePinnedMessagesDialog(false)) }
            )
        }

        if (state.showPermissionRationale) {
            PermissionRationaleDialog(
                onDismiss = { actions.onEvent(MessagesUiEvent.DismissPermissionRationale) },
                onConfirm = {
                    actions.onEvent(MessagesUiEvent.DismissPermissionRationale)
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
                selectedCount = totalDeletableCount,
                totalSelectedCount = totalSelectedCount,
                onConfirm = { actions.onEvent(MessagesUiEvent.ConfirmDeleteSelected) },
                onDismiss = { actions.onEvent(MessagesUiEvent.DismissDeleteConfirmation) }
            )
        }
    }
}
