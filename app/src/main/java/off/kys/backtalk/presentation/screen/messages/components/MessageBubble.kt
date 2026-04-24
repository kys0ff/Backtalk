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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A message bubble component that displays a message and its related metadata.
 * Supports replying, editing history, selection, and blinking animation.
 *
 * @param messageEntity The message entity to be displayed.
 * @param repliedMessageEntity The message entity that this message is replying to, if any.
 * @param blinkMessageId The ID of the message that should perform a blink animation.
 * @param isTop Whether this message is the first one in a consecutive group of messages from the same sender.
 * @param isBottom Whether this message is the last one in a consecutive group of messages from the same sender.
 * @param selectMode Whether the UI is currently in message selection mode.
 * @param isSelected Whether this specific message is currently selected.
 * @param onReplyPreviewClick Callback invoked when the replied message preview is clicked.
 * @param onClick Callback invoked when the message bubble is clicked.
 * @param onLongClick Callback invoked when the message bubble is long-pressed.
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
    var showExtraInfo by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val isBlinking = blinkMessageId == messageEntity.id
    val scale = remember { Animatable(1f) }
    val blinkAlpha = remember { Animatable(0f) }

    LaunchedEffect(isBlinking) {
        if (isBlinking) {
            repeat(2) {
                launch { scale.animateTo(1.05f, tween(180)); scale.animateTo(1f, tween(300)) }
                blinkAlpha.animateTo(1f, tween(180))
                blinkAlpha.animateTo(0f, tween(300))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isTop) 4.dp else 1.dp, bottom = if (isBottom) 4.dp else 1.dp),
        horizontalAlignment = Alignment.End
    ) {

        MessageSurface(
            isSelected = isSelected,
            isTop = isTop,
            isBottom = isBottom,
            blinkAlpha = blinkAlpha.value,
            scale = scale.value,
            modifier = Modifier.combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!selectMode) showExtraInfo = !showExtraInfo
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
        ) {
            MessageContent(
                message = messageEntity,
                repliedMessage = repliedMessageEntity,
                onReplyClick = onReplyPreviewClick,
                showOriginal = showExtraInfo
            )
        }

        MessageFooter(
            isVisible = showExtraInfo,
            timestamp = messageEntity.timestamp,
            editedAt = messageEntity.editedAt
        )
    }
}

/**
 * The outer surface of the message bubble, handling background color, shape, and animations.
 *
 * @param isSelected Whether the message is selected, affecting the bubble color.
 * @param isTop Used to determine the corner radius of the top-end.
 * @param isBottom Used to determine the corner radius of the bottom-end.
 * @param blinkAlpha The current alpha value for the highlight/blink effect.
 * @param scale The current scale value for the bubble animation.
 * @param modifier The modifier to be applied to the surface.
 * @param content The composable content to be displayed inside the bubble.
 */
@Composable
private fun MessageSurface(
    isSelected: Boolean,
    isTop: Boolean,
    isBottom: Boolean,
    blinkAlpha: Float,
    scale: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bubbleColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.primary

    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = if (isTop) 18.dp else 4.dp,
        bottomEnd = if (isBottom) 18.dp else 4.dp,
        bottomStart = 18.dp
    )

    Surface(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        color = bubbleColor,
        shape = shape,
        shadowElevation = 1.dp
    ) {
        Box {
            Surface(
                color = Color.White.copy(alpha = 0.3f * blinkAlpha),
                modifier = Modifier.matchParentSize()
            ) {}
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

/**
 * Displays the main content of the message, including replied message preview,
 * original text (if edited and expanded), the current text, and an "edited" tag.
 *
 * @param message The message entity containing the text and edit status.
 * @param repliedMessage The message being replied to, if any.
 * @param onReplyClick Callback when the reply preview is clicked.
 * @param showOriginal Whether to show the original message text if it has been edited.
 */
@Composable
private fun MessageContent(
    message: MessageEntity,
    repliedMessage: MessageEntity?,
    onReplyClick: () -> Unit,
    showOriginal: Boolean
) {
    val contentColor = contentColorFor(MaterialTheme.colorScheme.primary)

    if (repliedMessage != null) {
        ReplyPreview(text = repliedMessage.text, onPreviewClick = onReplyClick)
        Spacer(modifier = Modifier.height(4.dp))
    }

    if (message.editedText != null && showOriginal) {
        SmartText(
            text = message.text,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.6f),
            textDecoration = TextDecoration.LineThrough
        )
    }

    SmartText(
        text = message.editedText ?: message.text,
        color = contentColor,
        style = MaterialTheme.typography.bodyLarge
    )

    if (message.editedText != null) {
        Text(
            text = stringResource(R.string.edited),
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic,
            color = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * Displays the message metadata such as timestamp and edit time.
 * This footer is typically toggled by clicking the message bubble.
 *
 * @param isVisible Whether the footer is currently visible.
 * @param timestamp The original timestamp of the message.
 * @param editedAt The timestamp of when the message was last edited, if applicable.
 */
@Composable
private fun MessageFooter(
    isVisible: Boolean,
    timestamp: Long,
    editedAt: Long?
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        ) {
            Text(
                text = "${if (editedAt != null) stringResource(R.string.sent_at) else ""} ${
                    timeFormat.format(
                        Date(
                            timestamp
                        )
                    )
                }".trim(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            editedAt?.let {
                Text(
                    text = stringResource(R.string.edited_at, timeFormat.format(Date(it))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}