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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.state.MessagesUiState
import off.kys.backtalk.util.emptyString

/**
 * Composable function that displays the primary content of the messages screen, including
 * the list of messages and relevant dialogs for permissions and deletions.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param state The current UI state containing messages, selection data, and visibility flags for dialogs.
 * @param listState The [LazyListState] used to control and observe the scroll position of the message list.
 * @param onEditMessage Callback invoked when a message is selected for editing.
 * @param onReply Callback invoked when a user intends to reply to a specific message.
 * @param onToggleSelect Callback invoked to toggle the selection status of a message by its [MessageId].
 * @param onDismissRationale Callback to dismiss the permission rationale dialog.
 * @param onConfirmDelete Callback invoked to confirm and execute the deletion of selected messages.
 * @param onDismissDelete Callback to dismiss the delete confirmation dialog.
 */
@Composable
fun MessagesContent(
    modifier: Modifier,
    state: MessagesUiState,
    tags: List<String>,
    listState: LazyListState,
    onEditMessage: (MessageEntity?) -> Unit,
    onReply: (MessageEntity?) -> Unit,
    onToggleSelect: (MessageId) -> Unit,
    onDismissRationale: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onTagClick: (String) -> Unit,
    onNavigatePinned: () -> Unit,
    onTogglePinnedDialog: (Boolean) -> Unit,
    onTogglePin: (MessageEntity, Boolean) -> Unit,
    onScrollToMessage: (MessageId) -> Unit,
    onToggleImageSelect: (MessageId, String) -> Unit = { _, _ -> },
    totalDeletableCount: Int = 0,
    totalSelectedCount: Int = 0
) {
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

    Box(modifier = modifier.fillMaxSize()) {
        MessagesList(
            messages = state.filteredMessages,
            selectedMessageIds = state.selectedMessageIds,
            listState = listState,
            onEditMessage = onEditMessage,
            onReply = onReply,
            onToggleSelect = onToggleSelect,
            searchQuery = if (state.isSearchActive) state.searchQuery else emptyString(),
            onTagClick = onTagClick,
            blinkMessageId = state.blinkMessageId,
            onScrollToMessage = onScrollToMessage,
            selectedImagePaths = state.selectedImagePaths,
            onToggleImageSelect = onToggleImageSelect,
            onTogglePin = onTogglePin,
            contentPadding = PaddingValues(top = topPadding)
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
                    onClick = onNavigatePinned,
                    onListClick = { onTogglePinnedDialog(true) }
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
                    onTagClick = onTagClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.showPinnedMessagesDialog) {
            PinnedMessagesDialog(
                pinnedMessages = state.pinnedMessages,
                onMessageClick = { onScrollToMessage(it.id) },
                onUnpinClick = { onTogglePin(it, false) },
                onDismiss = { onTogglePinnedDialog(false) }
            )
        }

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
                selectedCount = totalDeletableCount,
                totalSelectedCount = totalSelectedCount,
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }
    }
}