package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Composable function that displays the messages list.
 *
 * @param messages The list of messages to display.
 * @param selectedMessageId The ID of the currently selected message.
 * @param onReply The callback function to handle replying to a message.
 * @param onSelect The callback function to handle selecting a message.
 */
@Composable
fun ColumnScope.MessagesList(
    messages: List<MessageEntity>,
    selectedMessageId: MessageId?,
    onReply: (MessageEntity?) -> Unit,
    onSelect: (MessageId?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (showTimestamp) {
                    TimestampHeader(current.timestamp)
                }

                SwipeToReplyWrapper(
                    onSwipe = {
                        onReply(
                            if (current.id == selectedMessageId) null else current
                        )
                    }
                ) {
                    MessageBubble(
                        messageEntity = current,
                        repliedMessageEntity = repliedMessage,
                        isTop = isTop,
                        isBottom = isBottom,
                        isSelected = selectedMessageId == current.id,
                        onClick = {
                            if (selectedMessageId != null) {
                                onSelect(
                                    if (selectedMessageId == current.id) null else current.id
                                )
                            }
                        },
                        onLongClick = {
                            onSelect(current.id)
                        }
                    )
                }
            }
        }
    }
}
