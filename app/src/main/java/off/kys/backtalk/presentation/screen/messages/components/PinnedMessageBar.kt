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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import kotlin.math.abs

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
                dotSize = 6.dp, // Slightly optimized for a sleek 56.dp bar look
                maxVisibleItems = 5,
                modifier = Modifier.padding(start = 16.dp)
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
fun VerticalPinnedIndicator(
    count: Int,
    activeIndex: Int,
    reverseDirection: Boolean,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 5,
    dotSize: Dp = 6.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
) {
    if (count <= 1) return

    val visualIndex = if (reverseDirection) (count - 1) - activeIndex else activeIndex

    val animatedIndex by animateFloatAsState(
        targetValue = visualIndex.coerceIn(0, maxOf(0, count - 1)).toFloat(),
        label = "ActiveIndexAnimation"
    )

    val density = LocalDensity.current
    val dotSizePx = with(density) { dotSize.toPx() }
    val spacingPx = dotSizePx * 0.7f
    val itemSpacePx = dotSizePx + spacingPx

    val totalVisibleHeightPx = (maxVisibleItems * itemSpacePx) - spacingPx
    val totalVisibleHeightDp = with(density) { totalVisibleHeightPx.toDp() }

    val halfVisible = (maxVisibleItems - 1) / 2f
    val maxScrollOffset = maxOf(0f, (count - maxVisibleItems).toFloat())
    val scrollOffset = (animatedIndex - halfVisible).coerceIn(0f, maxScrollOffset)

    Canvas(
        modifier = modifier.size(width = dotSize, height = totalVisibleHeightDp)
    ) {
        val viewportCenter = scrollOffset + halfVisible

        for (i in 0 until count) {
            val distanceFromCenter = abs(i - viewportCenter)
            val viewportScale = when {
                distanceFromCenter <= (maxVisibleItems - 3) / 2f -> 1f
                distanceFromCenter <= (maxVisibleItems - 1) / 2f -> {
                    1f - (distanceFromCenter - (maxVisibleItems - 3) / 2f) * 0.5f
                }
                distanceFromCenter <= (maxVisibleItems + 1) / 2f -> {
                    0.5f - (distanceFromCenter - (maxVisibleItems - 1) / 2f) * 0.5f
                }
                else -> 0f
            }.coerceIn(0f, 1f)

            val activeFraction = (1f - abs(i - animatedIndex)).coerceIn(0f, 1f)
            val scale = maxOf(viewportScale, activeFraction)

            if (scale <= 0f) continue

            val currentDotHeight = dotSizePx * (1f + 0.6f * activeFraction)

            val finalWidth = dotSizePx * scale
            val finalHeight = currentDotHeight * scale

            val centerX = size.width / 2f
            val centerY = (i * itemSpacePx) + (dotSizePx / 2f) - (scrollOffset * itemSpacePx)

            val color = lerp(trackColor, activeColor, activeFraction)

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = centerX - (finalWidth / 2f),
                    y = centerY - (finalHeight / 2f)
                ),
                size = Size(width = finalWidth, height = finalHeight),
                cornerRadius = CornerRadius(finalWidth / 2f, finalWidth / 2f)
            )
        }
    }
}