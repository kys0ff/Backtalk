package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.util.emptyString

/**
 * Composable function that displays the messages list.
 *
 * @param ColumnScope The scope for the column.
 * @param messages The list of messages to display.
 * @param selectedMessageIds The set of selected message IDs.
 * @param onEditMessage The callback function to handle editing a message.
 * @param onReply The callback function to handle replying to a message.
 * @param onToggleSelect The callback function to handle toggling the selection of a message.
 */
@Composable
fun ColumnScope.MessagesList(
    messages: List<MessageEntity>,
    selectedMessageIds: Set<MessageId>,
    listState: LazyListState,
    onEditMessage: (MessageEntity?) -> Unit,
    onReply: (MessageEntity?) -> Unit,
    onToggleSelect: (MessageId) -> Unit,
    searchQuery: String = emptyString()
) {
    val coroutineScope = rememberCoroutineScope()
    var blinkMessageId by remember { mutableStateOf<MessageId?>(null) }
    val selectionMode = selectedMessageIds.isNotEmpty()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f),
        contentPadding = PaddingValues(16.dp),
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

            val oneHourInMillis = 3600000L
            val canEdit = current.editedAt == null &&
                    (System.currentTimeMillis() - current.timestamp) < oneHourInMillis && current.voicePath == null

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                coroutineScope.launch {
                                    val targetIndex =
                                        reversed.indexOfFirst { it.id == id }
                                    if (targetIndex != -1) {
                                        listState.animateScrollToItem(targetIndex)
                                        blinkMessageId = id
                                        delay(1920)
                                        blinkMessageId = null
                                    }
                                }
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
                        highlightQuery = searchQuery
                    )
                }
            }
        }
    }
}