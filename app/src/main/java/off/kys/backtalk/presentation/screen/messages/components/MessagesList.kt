package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.model.MessageUiModel
import off.kys.backtalk.presentation.state.messages.MessageItemUiState
import off.kys.backtalk.presentation.state.messages.MessagesListActions
import off.kys.backtalk.presentation.state.messages.MessagesListUiState

@Composable
fun MessagesList(
    state: MessagesListUiState,
    actions: MessagesListActions,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.items.size) {
        if (state.items.isNotEmpty() && (listState.firstVisibleItemIndex <= 1)) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp) + contentPadding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        state = listState,
        reverseLayout = true
    ) {
        items(
            count = state.items.size,
            key = { state.items[it].message.id.value }
        ) { index ->
            MessageItemContainer(
                itemState = state.items[index],
                actions = actions,
                selectionMode = state.selectionMode,
                searchQuery = state.searchQuery,
                hapticFeedbackEnabled = state.hapticFeedbackEnabled,
                externalLinkWarningEnabled = state.externalLinkWarningEnabled,
                isContextMenuVisible = state.contextMenuEntityId == state.items[index].message.id,
                disableContextMenuOnLongClick = state.disableContextMenuOnLongClick
            )
        }
    }
}

@Composable
private fun LazyItemScope.MessageItemContainer(
    itemState: MessageItemUiState,
    actions: MessagesListActions,
    selectionMode: Boolean,
    searchQuery: String,
    hapticFeedbackEnabled: Boolean,
    externalLinkWarningEnabled: Boolean,
    isContextMenuVisible: Boolean,
    disableContextMenuOnLongClick: Boolean
) {
    val message = itemState.message

    Column(
        modifier = Modifier.animateItem(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (itemState.showTimestamp) {
            TimestampHeader(message.timestamp)
        }

        Box {
            SwipeToReplyWrapper(
                startIconRes = R.drawable.round_reply_24,
                onSwipeStart = { if (!selectionMode) actions.onReply(message) },
                endIconRes = R.drawable.round_edit_24,
                onSwipeEnd = if (message.canEdit && !selectionMode) {
                    { actions.onEdit(message) }
                } else null,
                showHint = itemState.showHint,
                onHintShown = actions::onMarkSwipeHintShown,
                hapticFeedbackEnabled = hapticFeedbackEnabled
            ) {
                MessageBubble(
                    message = message,
                    repliedMessage = itemState.repliedMessage,
                    blinkMessageId = if (itemState.isBlinking) message.id else null,
                    isTop = itemState.isTop,
                    isBottom = itemState.isBottom,
                    selectMode = selectionMode,
                    isSelected = itemState.isSelected,
                    onReplyPreviewClick = { message.repliedToId?.let(actions::onScrollToMessage) },
                    onClick = { if (selectionMode) actions.onToggleSelect(message.id) },
                    onLongClick = {
                        actions.onToggleSelect(message.id)
                        if (!selectionMode && !disableContextMenuOnLongClick) {
                            actions.onLongClick(message)
                        }
                    },
                    onDoubleClick = { if (!selectionMode) actions.onTogglePin(message) },
                    highlightQuery = searchQuery,
                    onTagClick = actions::onTagClick,
                    selectedImagePaths = itemState.selectedImagePaths,
                    onToggleImageSelect = { path -> actions.onToggleImageSelect(message.id, path) },
                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                    externalLinkWarningEnabled = externalLinkWarningEnabled
                )
            }

            if (isContextMenuVisible) {
                MessageContextMenu(
                    message = message,
                    onDismiss = { actions.onLongClick(null) },
                    onReply = { actions.onReply(message) },
                    onEdit = { actions.onEdit(message) },
                    onDelete = { actions.onDelete(message) },
                    onCopy = { actions.onCopy(message) }
                )
            }
        }
    }
}

object MessageItemUiMapper {
    fun map(
        messages: PersistentList<MessageUiModel>,
        repliedMessagesMap: PersistentMap<MessageId, MessageUiModel>,
        selectedMessageIds: PersistentSet<MessageId>,
        selectedImagePaths: PersistentMap<MessageId, PersistentSet<String>>,
        blinkMessageId: MessageId?,
        showHintForId: MessageId?
    ): PersistentList<MessageItemUiState> {
        val reversedMessages = messages.reversed()
        return reversedMessages.mapIndexed { index, current ->
            val next = reversedMessages.getOrNull(index - 1)
            val prev = reversedMessages.getOrNull(index + 1)

            MessageItemUiState(
                message = current,
                repliedMessage = repliedMessagesMap[current.id],
                isSelected = current.id in selectedMessageIds,
                selectedImagePaths = selectedImagePaths[current.id] ?: persistentSetOf(),
                showTimestamp = prev == null || current.timestamp - prev.timestamp > Constants.TIME_GAP_FOR_HEADER,
                isTop = prev == null || current.timestamp - prev.timestamp > Constants.TIME_GAP_FOR_GROUPING,
                isBottom = next == null || next.timestamp - current.timestamp > Constants.TIME_GAP_FOR_GROUPING,
                isBlinking = current.id == blinkMessageId,
                showHint = current.id == showHintForId
            )
        }.toPersistentList()
    }
}
