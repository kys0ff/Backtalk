package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.util.emptyString

@Composable
fun MessagesList(
    messages: List<MessageEntity>,
    selectedMessageIds: Set<MessageId>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onEditMessage: (MessageEntity?) -> Unit,
    onReply: (MessageEntity?) -> Unit,
    onToggleSelect: (MessageId) -> Unit,
    searchQuery: String = emptyString(),
    onTagClick: (String) -> Unit = {},
    blinkMessageId: MessageId? = null,
    onScrollToMessage: (MessageId) -> Unit = {},
    selectedImagePaths: Map<MessageId, Set<String>> = emptyMap(),
    onToggleImageSelect: (MessageId, String) -> Unit = { _, _ -> },
    onTogglePin: (MessageEntity, Boolean) -> Unit = { _, _ -> }
) {
    val selectionMode = selectedMessageIds.isNotEmpty() || selectedImagePaths.isNotEmpty()

    LaunchedEffect(messages.size) {
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
        val reversed = messages.reversed()

        items(
            count = reversed.size,
            key = { reversed[it].id() }
        ) { index ->
            val current = reversed[index]
            val next = reversed.getOrNull(index - 1)
            val prev = reversed.getOrNull(index + 1)

            val showTimestamp =
                prev == null || current.timestamp - prev.timestamp > Constants.TIME_GAP_FOR_HEADER

            val isTop =
                prev == null || current.timestamp - prev.timestamp > Constants.TIME_GAP_FOR_GROUPING

            val isBottom =
                next == null || next.timestamp - current.timestamp > Constants.TIME_GAP_FOR_GROUPING

            val repliedMessage =
                current.repliedToId?.let { id ->
                    messages.find { it.id == id }
                }

            val isSelected = current.id in selectedMessageIds

            val isLocked = (System.currentTimeMillis() - current.timestamp) >= Constants.MESSAGE_EDIT_DELETE_WINDOW

            val canEdit = current.editedAt == null &&
            !isLocked &&
            current.voicePath == null

            Column(
                modifier = Modifier.animateItem(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showTimestamp) {
                    TimestampHeader(current.timestamp)
                }

                SwipeToReplyWrapper(
                    onSwipeRight = if (canEdit) {
                        {
                            if (!selectionMode) {
                                onEditMessage(current)
                            }
                        }
                    } else null,
                    onSwipeLeft = {
                        if (!selectionMode) {
                            onReply(current)
                        }
                    },
                    leftIconRes = R.drawable.round_reply_24,
                    rightIconRes = R.drawable.round_edit_24
                ) {
                    MessageBubble(
                        messageEntity = current,
                        repliedMessageEntity = repliedMessage,
                        blinkMessageId = blinkMessageId,
                        isTop = isTop,
                        isBottom = isBottom,
                        selectMode = selectionMode,
                        isSelected = isSelected,
                        onReplyPreviewClick = {
                            current.repliedToId?.let { id ->
                                onScrollToMessage(id)
                            }
                        },
                        onClick = {
                            if (selectionMode) {
                                onToggleSelect(current.id)
                            }
                        },
                        onLongClick = {
                            onToggleSelect(current.id)
                        },
                        onDoubleClick = {
                            if (!selectionMode) {
                                onTogglePin(current, !current.isPinned)
                            }
                        },
                        highlightQuery = searchQuery,
                        onTagClick = onTagClick,
                        selectedImagePaths = selectedImagePaths[current.id] ?: emptySet(),
                        onToggleImageSelect = { path -> onToggleImageSelect(current.id, path) },
                        isLocked = isLocked
                    )
                }
            }
        }
    }
}
