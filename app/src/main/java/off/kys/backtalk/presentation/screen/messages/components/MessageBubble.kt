package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable function that displays a message bubble.
 *
 * @param messageEntity The message entity to display.
 * @param repliedMessageEntity The replied message entity, if any.
 * @param blinkMessageId The ID of the message to blink, if any.
 * @param isTop Whether the message is at the top of its group.
 * @param isBottom Whether the message is at the bottom of its group.
 * @param selectMode Whether select mode is enabled.
 * @param isSelected Whether the message is selected.
 * @param onReplyPreviewClick The callback function to handle clicks on the reply preview.
 * @param onClick The callback function to handle clicks on the message.
 * @param onLongClick The callback function to handle long clicks on the message.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    messageEntity: MessageEntity,
    repliedMessageEntity: MessageEntity?,
    blinkMessageId: MessageId?,
    isTop: Boolean,
    isBottom: Boolean,
    selectMode: Boolean,
    isSelected: Boolean,
    onReplyPreviewClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var showTime by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Remove ripple by passing null to indication
    val interactionSource = remember { MutableInteractionSource() }

    val baseBubbleColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = if (isTop) 18.dp else 4.dp,
        bottomEnd = if (isBottom) 18.dp else 4.dp,
        bottomStart = 18.dp
    )

    val isBlinking = blinkMessageId == messageEntity.id
    val blinkAlpha = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val blinkOverlayColor = Color.White.copy(
        alpha = 0.35f * blinkAlpha.value
    )


    LaunchedEffect(isBlinking) {
        if (isBlinking) {
            blinkAlpha.snapTo(0f)
            scale.snapTo(1f)

            repeat(2) {
                // Blink in + scale up
                blinkAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(180)
                )
                scale.animateTo(
                    targetValue = 1.05f,
                    animationSpec = tween(180)
                )

                // Blink out + scale back
                blinkAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(300)
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300)
                )
            }

            blinkAlpha.snapTo(0f)
            scale.snapTo(1f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isTop) 6.dp else 0.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null, // This removes the ripple effect
                    onClick = {
                        if (!selectMode) {
                            if (!isSelected) showTime = !showTime
                        }
                        onClick()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                ),
            color = baseBubbleColor,
            shape = bubbleShape
        ) {
            Surface(
                color = blinkOverlayColor,
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
                ) {
                    if (repliedMessageEntity != null) {
                        ReplyPreview(
                            text = repliedMessageEntity.text,
                            onPreviewClick = onReplyPreviewClick
                        )
                    }

                    Text(
                        text = messageEntity.text,
                        color = contentColorFor(baseBubbleColor)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showTime,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = SimpleDateFormat(
                    "h:mm a",
                    Locale.getDefault()
                ).format(Date(messageEntity.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, end = 4.dp)
            )
        }
    }
}