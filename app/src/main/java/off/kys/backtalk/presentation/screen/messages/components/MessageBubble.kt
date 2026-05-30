package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    selectedImagePaths: Set<String> = emptySet(),
    onReplyPreviewClick: () -> Unit,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit = {},
    onLongClick: () -> Unit,
    onToggleImageSelect: (String) -> Unit = {},
    highlightQuery: String? = null,
    onTagClick: (String) -> Unit = {},
    isLocked: Boolean = false
) {
    var showExtraInfo by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val preferences = koinInject<BacktalkPreferences>()
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

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

    // Determine if the content only consists of images (no text, no voice, no replies, no active metadata tags)
    val hasImages =
        !messageEntity.mediaPath.isNullOrEmpty() || !messageEntity.mediaPaths.isNullOrEmpty()
    val hasText = (messageEntity.editedText ?: messageEntity.text).isNotEmpty()
    val hasVoice = messageEntity.voicePath != null
    val hasRepliedMessage = repliedMessageEntity != null
    val hasTags = messageEntity.isReminder || messageEntity.isPinned
    val isImageOnly = hasImages && !hasText && !hasVoice && !hasRepliedMessage && !hasTags

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
            isImageOnly = isImageOnly,
            modifier = Modifier
                .wrapContentWidth()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (!selectMode) showExtraInfo = !showExtraInfo
                        onClick()
                    },
                    onDoubleClick = {
                        if (!selectMode) {
                            scope.launch {
                                scale.animateTo(0.92f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
                                scale.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium))
                            }
                            if (preferences.hapticFeedbackEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onDoubleClick()
                        }
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
                onTagClick = onTagClick,
                selectedImagePaths = selectedImagePaths,
                onToggleImageSelect = onToggleImageSelect
            )
        }

        MessageFooter(
            isVisible = showExtraInfo,
            timestamp = messageEntity.timestamp,
            editedAt = messageEntity.editedAt,
            isReminder = messageEntity.isReminder,
            originalTimestamp = messageEntity.originalCreationTimestamp,
            targetTimestamp = messageEntity.scheduledTimestamp,
            isLocked = isLocked
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
    isImageOnly: Boolean,
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
            width = 1.dp,
            color = contentColorFor(baseColor).copy(alpha = 0.5f)
        )
    } else null

    val horizontalPadding = if (isImageOnly) 4.dp else 12.dp
    val verticalPadding = if (isImageOnly) 4.dp else 8.dp

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
            Column(
                modifier = Modifier.padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                )
            ) {
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
    selectedImagePaths: Set<String>,
    onToggleImageSelect: (String) -> Unit,
    highlightQuery: String? = null,
    onTagClick: (String) -> Unit = {}
) {
    val navigator = LocalNavigator.current
    val contentColor = contentColorFor(MaterialTheme.colorScheme.primary)

    AnimatedVisibility(
        visible = message.isReminder || message.isPinned,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (message.isReminder) {
                    ReminderTag(contentColor)
                }
                AnimatedVisibility(
                    visible = message.isPinned,
                    enter = fadeIn() + scaleIn(initialScale = 0.7f, animationSpec = spring(Spring.DampingRatioMediumBouncy)),
                    exit = fadeOut() + scaleOut()
                ) {
                    PinTag(contentColor)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
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
        StaggeredImageGrid(
            images = images,
            selectedImages = selectedImagePaths,
            onImageClick = { imagePath ->
                if (selectedImagePaths.isNotEmpty()) {
                    onToggleImageSelect(imagePath)
                } else {
                    navigator?.push(ImagePreviewScreen(imagePath))
                }
            },
            onImageLongClick = { imagePath -> onToggleImageSelect(imagePath) }
        )
        val messageText = message.editedText ?: message.text
        if (messageText.isNotEmpty() || message.voicePath != null) {
            Spacer(modifier = Modifier.height(4.dp))
        }
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
fun StaggeredImageGrid(
    modifier: Modifier = Modifier,
    images: List<String>,
    selectedImages: Set<String> = emptySet(),
    onImageClick: (String) -> Unit,
    onImageLongClick: (String) -> Unit = {}
) {
    if (images.isEmpty()) return

    val gridWidth = 280.dp
    val spacing = 6.dp
    val containerModifier = modifier
        .width(gridWidth)
        .clip(MaterialTheme.shapes.large)

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        when (images.size) {
            1 -> {
                GridImage(
                    path = images[0],
                    isSelected = images[0] in selectedImages,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .widthIn(max = gridWidth)
                        .heightIn(max = 360.dp),
                    imageModifier = Modifier,
                    onClick = onImageClick,
                    onLongClick = onImageLongClick,
                    contentScale = ContentScale.Fit
                )
            }

            2 -> {
                Row(
                    modifier = containerModifier,
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    images.forEach { path ->
                        GridImage(
                            path = path,
                            isSelected = path in selectedImages,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.75f),
                            onClick = onImageClick,
                            onLongClick = onImageLongClick
                        )
                    }
                }
            }

            3 -> {
                Row(
                    modifier = containerModifier,
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    GridImage(
                        path = images[0],
                        isSelected = images[0] in selectedImages,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f),
                        onClick = onImageClick,
                        onLongClick = onImageLongClick
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        GridImage(
                            path = images[1],
                            isSelected = images[1] in selectedImages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .aspectRatio(1.5f),
                            onClick = onImageClick,
                            onLongClick = onImageLongClick
                        )
                        GridImage(
                            path = images[2],
                            isSelected = images[2] in selectedImages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .aspectRatio(1.5f),
                            onClick = onImageClick,
                            onLongClick = onImageLongClick
                        )
                    }
                }
            }

            else -> {
                Row(
                    modifier = containerModifier,
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        GridImage(
                            path = images[0],
                            isSelected = images[0] in selectedImages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            onClick = onImageClick,
                            onLongClick = onImageLongClick
                        )
                        GridImage(
                            path = images[2],
                            isSelected = images[2] in selectedImages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f),
                            onClick = onImageClick,
                            onLongClick = onImageLongClick
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        GridImage(
                            path = images[1],
                            isSelected = images[1] in selectedImages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f),
                            onClick = onImageClick,
                            onLongClick = onImageLongClick
                        )
                        GridImage(
                            path = images[3],
                            isSelected = images[3] in selectedImages,
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(1f),
                            onClick = onImageClick,
                            onLongClick = onImageLongClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridImage(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier.fillMaxSize(),
    path: String,
    isSelected: Boolean = false,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit = {},
    contentScale: ContentScale = ContentScale.Crop
) {
    val haptic = LocalHapticFeedback.current
    val preferences = koinInject<BacktalkPreferences>()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onClick(path) },
                onLongClick = {
                    if (preferences.hapticFeedbackEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onLongClick(path)
                }
            )
    ) {
        AsyncImage(
            modifier = imageModifier,
            model = ImageRequest.Builder(context)
                .data(File(path))
                .crossfade(true)
                .build(),
            contentDescription = null,
            placeholder = painterResource(R.drawable.round_progress_activity_24px),
            error = painterResource(R.drawable.round_broken_image_24px),
            contentScale = contentScale,
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(width = 1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_check_24),
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
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
    targetTimestamp: Long? = null,
    isLocked: Boolean = false
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
            if (isLocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "History locked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontStyle = FontStyle.Italic
                    )
                    Icon(
                        painter = painterResource(R.drawable.round_lock_24),
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
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