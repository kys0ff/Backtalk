package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * An expressive, floating bar displaying the currently active pinned message
 * with fluid transitions, Material 3 surface tinting, and a Telegram-style
 * vertical progress indicator.
 */
@Composable
fun PinnedMessageBar(
    modifier: Modifier = Modifier,
    pinnedMessages: List<MessageEntity>,
    activeIndex: Int,
    onClick: () -> Unit,
    onListClick: () -> Unit
) {
    if (pinnedMessages.isEmpty()) return

    val currentPinned = pinnedMessages.getOrNull(activeIndex) ?: return
    val labelText = if (pinnedMessages.size > 1) {
        "Pinned Message (${activeIndex + 1}/${pinnedMessages.size})"
    } else {
        "Pinned Message"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(
                    onClick = onClick,
                    role = Role.Button,
                    onClickLabel = "Jump to pinned message"
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VerticalPinnedIndicator(
                count = pinnedMessages.size,
                activeIndex = activeIndex,
                reverseDirection = true,
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    .width(3.dp)
                    .fillMaxHeight()
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = labelText,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "PinnedLabelAnimation"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                AnimatedContent(
                    targetState = currentPinned,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "PinnedMessageAnimation"
                ) { message ->
                    SmartText(
                        text = message.editedText ?: message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onListClick,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_pinboard_24px),
                    contentDescription = "View all ${pinnedMessages.size} pinned messages",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VerticalPinnedIndicator(
    count: Int,
    activeIndex: Int,
    reverseDirection: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
) {
    val visualIndex = if (reverseDirection) {
        (count - 1) - activeIndex
    } else {
        activeIndex
    }

    val animatedIndexOffset by animateFloatAsState(
        targetValue = visualIndex.coerceIn(0, maxOf(0, count - 1)).toFloat(),
        label = "VerticalIndicatorAnimation"
    )

    Canvas(modifier = modifier) {
        val canvasHeight = size.height
        val canvasWidth = size.width
        val cornerRadius = CornerRadius(canvasWidth / 2, canvasWidth / 2)

        if (count <= 1) {
            drawRoundRect(
                color = activeColor,
                size = size,
                cornerRadius = cornerRadius
            )
        } else {
            drawRoundRect(
                color = trackColor,
                size = size,
                cornerRadius = cornerRadius
            )

            val segmentHeight = canvasHeight / count
            val topOffset = animatedIndexOffset * segmentHeight

            drawRoundRect(
                color = activeColor,
                topLeft = Offset(x = 0f, y = topOffset),
                size = Size(width = canvasWidth, height = segmentHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}