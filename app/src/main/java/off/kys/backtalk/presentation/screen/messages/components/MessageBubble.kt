package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.screen.preview.ImagePreviewScreen
import off.kys.backtalk.util.emptyString
import org.koin.compose.koinInject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A message bubble component that displays a message and its related metadata.
 * Supports replying, editing history, selection, and blinking animation.
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
    onLongClick: () -> Unit,
    highlightQuery: String? = null,
    onTagClick: (String) -> Unit = {}
) {
    var showExtraInfo by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val preferences = koinInject<BacktalkPreferences>()
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
            isReminder = messageEntity.isReminder,
            blinkAlpha = blinkAlpha.value,
            scale = scale.value,
            modifier = Modifier
                .wrapContentWidth()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (!selectMode) showExtraInfo = !showExtraInfo
                        onClick()
                    },
                    onLongClick = {
                        if (preferences.hapticFeedbackEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onLongClick()
                    }
                )
        ) {
            MessageContent(
                message = messageEntity,
                repliedMessage = repliedMessageEntity,
                onReplyClick = onReplyPreviewClick,
                showOriginal = showExtraInfo,
                highlightQuery = highlightQuery,
                onTagClick = onTagClick
            )
        }

        MessageFooter(
            isVisible = showExtraInfo,
            timestamp = messageEntity.timestamp,
            editedAt = messageEntity.editedAt,
            isReminder = messageEntity.isReminder,
            originalTimestamp = messageEntity.originalCreationTimestamp,
            targetTimestamp = messageEntity.scheduledTimestamp
        )
    }
}

@Composable
private fun MessageSurface(
    isSelected: Boolean,
    isTop: Boolean,
    isBottom: Boolean,
    isReminder: Boolean,
    blinkAlpha: Float,
    scale: Float,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.primary

    val bubbleColor = if (isReminder && !isSelected) {
        baseColor.copy(alpha = 0.9f)
    } else {
        baseColor
    }

    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = if (isTop) 18.dp else 4.dp,
        bottomEnd = if (isBottom) 18.dp else 4.dp,
        bottomStart = 18.dp
    )

    val border = if (isReminder) {
        BorderStroke(
            1.dp,
            contentColorFor(baseColor).copy(alpha = 0.5f)
        )
    } else null

    Surface(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        color = bubbleColor,
        shape = shape,
        border = border,
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

@Composable
private fun MessageContent(
    message: MessageEntity,
    repliedMessage: MessageEntity?,
    onReplyClick: () -> Unit,
    showOriginal: Boolean,
    highlightQuery: String? = null,
    onTagClick: (String) -> Unit = {}
) {
    val navigator = LocalNavigator.currentOrThrow
    val contentColor = contentColorFor(MaterialTheme.colorScheme.primary)

    if (message.isReminder || message.isPinned) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (message.isReminder) {
                ReminderTag(contentColor)
            }
            if (message.isPinned) {
                PinTag(contentColor)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }

    if (repliedMessage != null) {
        ReplyPreview(
            text = if (repliedMessage.voicePath != null) stringResource(R.string.chat_voice_message) else if (repliedMessage.mediaPath != null || !repliedMessage.mediaPaths.isNullOrEmpty()) "[Image]" else repliedMessage.text,
            voicePath = repliedMessage.voicePath,
            onPreviewClick = onReplyClick
        )
        Spacer(modifier = Modifier.height(4.dp))
    }

    val images = remember(message) {
        val list = mutableListOf<String>()
        message.mediaPath?.let { list.add(it) }
        message.mediaPaths?.let { list.addAll(it) }
        list
    }

    if (images.isNotEmpty()) {
        ImageGrid(images) { imagePath -> navigator += ImagePreviewScreen(imagePath) }
        Spacer(modifier = Modifier.height(4.dp))
    }

    if (message.voicePath != null) {
        VoiceMessageBubble(
            voicePath = message.voicePath,
            duration = message.voiceDuration ?: 0L,
            waveformData = message.waveformData ?: emptyList(),
            contentColor = contentColor
        )
    } else {
        val messageText = message.editedText ?: message.text
        if (messageText.isNotEmpty()) {
            if (message.editedText != null && showOriginal) {
                SmartText(
                    text = message.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.6f),
                    textDecoration = TextDecoration.LineThrough,
                    highlightQuery = highlightQuery,
                    onMentionClicked = onTagClick
                )
            }

            SmartText(
                text = messageText,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge,
                highlightQuery = highlightQuery,
                onMentionClicked = onTagClick
            )
        }

        if (message.editedText != null) {
            Text(
                text = stringResource(R.string.chat_status_edited),
                style = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                color = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ImageGrid(images: List<String>, onImageClick: (String) -> Unit) {
    val containerModifier = Modifier
        .clip(MaterialTheme.shapes.medium)

    when (images.size) {
        1 -> {
            val path = images[0]
            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = containerModifier
                    .sizeIn(
                        minWidth = 120.dp,
                        maxWidth = 260.dp,
                        minHeight = 120.dp,
                        maxHeight = 320.dp
                    )
                    .aspectRatio(4f / 3f)
                    .clickable { onImageClick(path) },
                contentScale = ContentScale.Crop
            )
        }

        2 -> {
            Row(
                modifier = containerModifier.width(260.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                images.forEach { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onImageClick(path) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        3 -> {
            Column(
                modifier = containerModifier.width(260.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val firstPath = images[0]
                AsyncImage(
                    model = File(firstPath),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { onImageClick(firstPath) },
                    contentScale = ContentScale.Crop
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    images.drop(1).forEach { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { onImageClick(path) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = containerModifier.width(260.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                images.take(4).chunked(2).forEach { rowImages ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        rowImages.forEach { path ->
                            AsyncImage(
                                model = File(path),
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { onImageClick(path) },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderTag(contentColor: Color) {
    Surface(
        color = contentColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.round_access_alarm_24),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = stringResource(R.string.chat_reminder_tag),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun PinTag(contentColor: Color) {
    Surface(
        color = contentColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.round_push_pin_24),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = "Pinned",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun MessageFooter(
    isVisible: Boolean,
    timestamp: Long,
    editedAt: Long?,
    isReminder: Boolean = false,
    originalTimestamp: Long? = null,
    targetTimestamp: Long? = null
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
            if (isReminder && originalTimestamp != null && targetTimestamp != null) {
                Text(
                    text = "${stringResource(R.string.chat_reminder_original_time)} ${
                        timeFormat.format(
                            Date(originalTimestamp)
                        )
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${stringResource(R.string.chat_reminder_target_time)} ${
                        timeFormat.format(
                            Date(targetTimestamp)
                        )
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Text(
                    text = "${if (editedAt != null) stringResource(R.string.chat_sent_at) else emptyString()} ${
                        timeFormat.format(
                            Date(
                                timestamp
                            )
                        )
                    }".trim(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            editedAt?.let {
                Text(
                    text = stringResource(R.string.chat_edited_at, timeFormat.format(Date(it))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}