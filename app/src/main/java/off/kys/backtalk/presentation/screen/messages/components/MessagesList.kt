package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.model.MessageUiModel
import off.kys.backtalk.util.emptyString
import org.koin.compose.koinInject

@Composable
fun MessagesList(
    messages: PersistentList<MessageUiModel>,
    repliedMessagesMap: PersistentMap<MessageId, MessageUiModel>,
    selectedMessageIds: PersistentSet<MessageId>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onEditMessage: (MessageUiModel?) -> Unit,
    onReply: (MessageUiModel?) -> Unit,
    onToggleSelect: (MessageId) -> Unit,
    onDeleteMessage: (MessageUiModel) -> Unit = {},
    onCopyMessage: (MessageUiModel) -> Unit = {},
    contextMenuEntity: MessageUiModel? = null,
    searchQuery: String = emptyString(),
    onTagClick: (String) -> Unit = {},
    blinkMessageId: MessageId? = null,
    onScrollToMessage: (MessageId) -> Unit = {},
    selectedImagePaths: PersistentMap<MessageId, PersistentSet<String>> = persistentMapOf(),
    onToggleImageSelect: (MessageId, String) -> Unit = { _, _ -> },
    onTogglePin: (MessageUiModel, Boolean) -> Unit = { _, _ -> },
    onLongClick: (MessageUiModel?) -> Unit = {}
) {
    val preferences = koinInject<BacktalkPreferences>()
    var showHintForId by remember { mutableStateOf<MessageId?>(null) }

    val selectionMode = selectedMessageIds.isNotEmpty() || selectedImagePaths.isNotEmpty()

    val reversedMessages = remember(messages) { messages.reversed() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !preferences.swipeHintShown) {
            val hintMessage = messages.lastOrNull { message -> message.canEdit }
            showHintForId = hintMessage?.id
        }

        if (messages.isNotEmpty() && listState.firstVisibleItemIndex <= 1) {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp) + contentPadding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        state = listState,
        reverseLayout = true
    ) {
        items(
            count = reversedMessages.size,
            key = { reversedMessages[it].id.value }
        ) { index ->
            val current = reversedMessages[index]
            val next = reversedMessages.getOrNull(index - 1)
            val prev = reversedMessages.getOrNull(index + 1)

            val showTimestamp =
                prev == null || current.timestamp - prev.timestamp > Constants.TIME_GAP_FOR_HEADER

            val isTop =
                prev == null || current.timestamp - prev.timestamp > Constants.TIME_GAP_FOR_GROUPING

            val isBottom =
                next == null || next.timestamp - current.timestamp > Constants.TIME_GAP_FOR_GROUPING

            val repliedMessage = repliedMessagesMap[current.id]

            val isSelected = current.id in selectedMessageIds

            val currentOnReply = remember(current) { { onReply(current) } }
            val currentOnEdit = remember(current) { { onEditMessage(current) } }
            val currentOnLongClick = remember(current) { { onLongClick(current) } }
            val currentOnTogglePin = remember(current) { { onTogglePin(current, !current.isPinned) } }
            val currentOnToggleSelect = remember(current) { { onToggleSelect(current.id) } }
            val currentOnScrollToMessage = remember(current) {
                { current.repliedToId?.let { onScrollToMessage(it) } ?: Unit }
            }
            val currentOnToggleImageSelect = remember(current) {
                { path: String -> onToggleImageSelect(current.id, path) }
            }

            Column(
                modifier = Modifier.animateItem(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showTimestamp) {
                    TimestampHeader(current.timestamp)
                }

                Box {
                    SwipeToReplyWrapper(
                        startIconRes = R.drawable.round_reply_24,
                        onSwipeStart = {
                            if (!selectionMode) {
                                currentOnReply()
                            }
                        },
                        endIconRes = R.drawable.round_edit_24,
                        onSwipeEnd = if (current.canEdit) {
                            {
                                if (!selectionMode) {
                                    currentOnEdit()
                                }
                            }
                        } else null,
                        showHint = showHintForId == current.id,
                        onHintShown = {
                            showHintForId = null
                            preferences.swipeHintShown = true
                        }
                    ) {
                        MessageBubble(
                            message = current,
                            repliedMessage = repliedMessage,
                            blinkMessageId = blinkMessageId,
                            isTop = isTop,
                            isBottom = isBottom,
                            selectMode = selectionMode,
                            isSelected = isSelected,
                            onReplyPreviewClick = currentOnScrollToMessage,
                            onClick = {
                                if (selectionMode) {
                                    currentOnToggleSelect()
                                }
                            },
                            onLongClick = {
                                currentOnToggleSelect()
                                if (!selectionMode) {
                                    currentOnLongClick()
                                }
                            },
                            onDoubleClick = {
                                if (!selectionMode) {
                                    currentOnTogglePin()
                                }
                            },
                            highlightQuery = searchQuery,
                            onTagClick = onTagClick,
                            selectedImagePaths = selectedImagePaths[current.id] ?: persistentSetOf(),
                            onToggleImageSelect = currentOnToggleImageSelect,
                            hapticFeedbackEnabled = preferences.hapticFeedbackEnabled
                        )
                    }

                    if (contextMenuEntity?.id == current.id) {
                        MessageContextMenu(
                            message = current,
                            onDismiss = { onLongClick(null) },
                            onReply = { onReply(current) },
                            onEdit = { onEditMessage(current) },
                            onDelete = { onDeleteMessage(current) },
                            onCopy = { onCopyMessage(current) }
                        )
                    }
                }
            }
        }
    }
}
