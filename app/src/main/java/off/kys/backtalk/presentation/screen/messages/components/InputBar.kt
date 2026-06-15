package off.kys.backtalk.presentation.screen.messages.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import off.kys.backtalk.R
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.presentation.components.HintTooltip
import off.kys.backtalk.presentation.event.InputBarEvent
import off.kys.backtalk.presentation.viewmodel.InputBarEffect
import off.kys.backtalk.presentation.viewmodel.InputBarViewModel
import off.kys.backtalk.util.getFirstLinkOrNull
import off.kys.backtalk.util.toast
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    viewModel: InputBarViewModel,
    messageInput: String,
    replyingTo: MessageEntity?,
    editingMessage: MessageEntity?,
    sharedImageUris: List<String> = emptyList(),
    onCancelSharedImage: () -> Unit = {},
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val preferences = koinInject<BacktalkPreferences>()
    val layoutDirection = LocalLayoutDirection.current
    val state by viewModel.uiState.collectAsState()

    val shakeOffset = remember { Animatable(0f) }

    val datePickerState = rememberDatePickerState(
        selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val todayUtc = Instant.now()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant()
                        .toEpochMilli()
                    return utcTimeMillis >= todayUtc
                }
            }
        }
    )
    val timePickerState = rememberTimePickerState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.onEvent(InputBarEvent.StartRecording)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InputBarEffect.TriggerShake -> {
                    repeat(4) { index ->
                        shakeOffset.animateTo(
                            targetValue = if (index % 2 == 0) 15f else -15f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                                stiffness = Spring.StiffnessHigh
                            )
                        )
                    }
                    shakeOffset.animateTo(0f)
                }
            }
        }
    }

    LaunchedEffect(replyingTo) {
        viewModel.onEvent(InputBarEvent.UpdateReplyingTo(replyingTo))
    }
    LaunchedEffect(editingMessage) {
        viewModel.onEvent(InputBarEvent.UpdateEditingMessage(editingMessage))
    }
    LaunchedEffect(messageInput) {
        if (messageInput != state.textFieldState.text.toString()) {
            state.textFieldState.setTextAndPlaceCursorAtEnd(messageInput)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.onEvent(InputBarEvent.RequestExactAlarmPermission)
    }

    fun handleScheduleClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onEvent(InputBarEvent.RequestExactAlarmPermission)
            }
        } else {
            viewModel.onEvent(InputBarEvent.RequestExactAlarmPermission)
        }
    }

    fun startRecordingInternal() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.onEvent(InputBarEvent.StartRecording)
        } else {
            viewModel.onEvent(InputBarEvent.ShowTapHint)
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    MessageSchedulingDialogs(
        stage = state.schedulingStage,
        datePickerState = datePickerState,
        timePickerState = timePickerState,
        onStageChange = { nextStage ->
            viewModel.onEvent(
                InputBarEvent.ChangeSchedulingStage(
                    nextStage
                )
            )
        },
        onSchedule = { time ->
            viewModel.onEvent(
                InputBarEvent.ScheduleMessage(
                    state.textFieldState.text.toString(),
                    time
                )
            )
            context.toast(R.string.message_scheduled_success)
        }
    )

    Surface(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.union(WindowInsets.ime).only(WindowInsetsSides.Bottom)
        ),
        tonalElevation = 2.dp
    ) {
        Column(modifier = modifier) {
            InputBarReplyHeader(
                replyingTo = state.replyingTo,
                editingMessage = state.editingMessage,
                onCancelReply = { viewModel.onEvent(InputBarEvent.CancelReply) },
                onCancelEdit = { viewModel.onEvent(InputBarEvent.CancelEdit) }
            )

            LinkPreviewSection(
                text = state.textFieldState.text.toString(),
                previewEnabled = state.linkPreviewEnabled
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttachButtonVisibility(
                    isVisible = !state.isRecording,
                    onAttachClick = { viewModel.onEvent(InputBarEvent.AttachClicked) }
                )

                ChatTextField(
                    modifier = Modifier.weight(1f),
                    textFieldState = state.textFieldState,
                    isRecording = state.isRecording,
                    amplitudes = state.amplitudes,
                    durationText = state.durationText,
                    sendWithEnter = preferences.sendWithEnter,
                    onSend = {
                        viewModel.onEvent(InputBarEvent.SendMessage(state.textFieldState.text.toString()))
                    },
                    onContentReceived = { transferableContent ->
                        viewModel.onEvent(InputBarEvent.ContentReceived(transferableContent))
                        null
                    }
                )

                ActionButtons(
                    showSend = state.isSendButtonVisible,
                    onSendClick = {
                        viewModel.onEvent(InputBarEvent.SendMessage(state.textFieldState.text.toString()))
                    },
                    onScheduleClick = {
                        if (state.textFieldState.text.isNotBlank() && state.editingMessage == null) {
                            if (preferences.hapticFeedbackEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            handleScheduleClick()
                        }
                    },
                    showTapHint = state.showTapHint,
                    shakeOffset = shakeOffset.value,
                    offsetX = state.offsetX,
                    onShowTapHint = {
                        viewModel.onEvent(InputBarEvent.ShowTapHint)
                    },
                    onDragStart = {
                        viewModel.onEvent(InputBarEvent.ClearTapHint)
                        startRecordingInternal()
                    },
                    onDrag = { change, dragAmount ->
                        if (state.isRecording) {
                            change.consume()
                            val newOffsetX = if (layoutDirection == LayoutDirection.Rtl) {
                                (state.offsetX + dragAmount.x).coerceAtLeast(0f)
                            } else {
                                (state.offsetX + dragAmount.x).coerceAtMost(0f)
                            }
                            viewModel.onEvent(InputBarEvent.UpdateOffsetX(newOffsetX))

                            if (layoutDirection == LayoutDirection.Rtl && newOffsetX > 300f) {
                                viewModel.onEvent(InputBarEvent.CancelRecording)
                                viewModel.onEvent(InputBarEvent.UpdateOffsetX(0f))
                            } else if (layoutDirection != LayoutDirection.Rtl && newOffsetX < -300f) {
                                viewModel.onEvent(InputBarEvent.CancelRecording)
                                viewModel.onEvent(InputBarEvent.UpdateOffsetX(0f))
                            }
                        }
                    },
                    onDragEnd = {
                        if (state.isRecording) {
                            viewModel.onEvent(InputBarEvent.StopAndSendRecording)
                        }
                        viewModel.onEvent(InputBarEvent.UpdateOffsetX(0f))
                    },
                    onDragCancel = {
                        if (state.isRecording) {
                            viewModel.onEvent(InputBarEvent.CancelRecording)
                        }
                        viewModel.onEvent(InputBarEvent.UpdateOffsetX(0f))
                    }
                )
            }

            AnimatedVisibility(
                visible = state.textFieldState.selection.length > 0,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                FormattingToolbar(onFormattingClick = { startSym, endSym ->
                    state.textFieldState.edit {
                        val currentSelection = selection
                        val selectedText =
                            toString().substring(currentSelection.start, currentSelection.end)
                        replace(
                            currentSelection.start,
                            currentSelection.end,
                            "$startSym$selectedText$endSym"
                        )
                        val newCursorPos =
                            currentSelection.start + startSym.length + selectedText.length + endSym.length
                        selection = TextRange(newCursorPos)
                    }
                })
            }
        }
    }

    if (state.showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { viewModel.onEvent(InputBarEvent.DismissPermissionRationale) },
            onConfirm = {
                viewModel.onEvent(InputBarEvent.DismissPermissionRationale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
        )
    }

    if (sharedImageUris.isNotEmpty()) {
        SharedImageDialog(
            uris = sharedImageUris,
            onDismiss = onCancelSharedImage,
            onSend = { caption ->
                viewModel.onEvent(InputBarEvent.SendSharedImages(sharedImageUris, caption))
                onCancelSharedImage()
            }
        )
    }
}

@Composable
private fun LinkPreviewSection(text: String, previewEnabled: Boolean) {
    val firstUrl = remember(text, previewEnabled) {
        if (previewEnabled) text.getFirstLinkOrNull() else null
    }
    AnimatedVisibility(
        visible = firstUrl != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        if (firstUrl != null) {
            Column {
                Spacer(Modifier.size(8.dp))
                Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp)) {
                    LinkPreviewCard(url = firstUrl)
                }
            }
        }
    }
}

@Composable
private fun AttachButtonVisibility(isVisible: Boolean, onAttachClick: () -> Unit) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandHorizontally() + fadeIn(),
        exit = shrinkHorizontally() + fadeOut()
    ) {
        HintTooltip(stringResource(R.string.common_attach)) {
            IconButton(onClick = onAttachClick) {
                Icon(
                    painter = painterResource(R.drawable.round_add_photo_alternate_24),
                    contentDescription = stringResource(R.string.common_attach),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ChatTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    isRecording: Boolean,
    amplitudes: List<Float>,
    durationText: String,
    sendWithEnter: Boolean,
    onSend: () -> Unit,
    onContentReceived: ((TransferableContent) -> TransferableContent?)? = null
) {
    Box(
        modifier = modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    ) {
        AnimatedVisibility(
            visible = !isRecording,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            TextField(
                state = textFieldState,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .then(
                        if (onContentReceived != null) {
                            Modifier.contentReceiver(onContentReceived)
                        } else Modifier
                    ),
                textStyle = TextStyle(
                    textDirection = TextDirection.Content
                ),
                placeholder = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(stringResource(R.string.chat_input_hint))
                    }
                },
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = if (sendWithEnter) ImeAction.Send else ImeAction.Default
                ),
                onKeyboardAction = { onSend() },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
        }

        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            VoiceRecordingIndicator(
                amplitudes = amplitudes,
                durationText = durationText
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionButtons(
    showSend: Boolean,
    onSendClick: () -> Unit,
    onScheduleClick: () -> Unit,
    showTapHint: Boolean,
    shakeOffset: Float,
    offsetX: Float,
    onShowTapHint: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (PointerInputChange, Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val sendButtonInteractionSource = remember { MutableInteractionSource() }
    val isPressed by sendButtonInteractionSource.collectIsPressedAsState()
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    val sendButtonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SendButtonScale"
    )

    AnimatedContent(targetState = showSend, label = "SendVoiceToggle") { targetShowSend ->
        if (targetShowSend) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(sendButtonScale)
                    .combinedClickable(
                        onClick = onSendClick,
                        onLongClick = onScheduleClick,
                        interactionSource = sendButtonInteractionSource,
                        indication = ripple(bounded = false, radius = 24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_send_24),
                    contentDescription = stringResource(R.string.common_send),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val visualX = if (layoutDirection == LayoutDirection.Rtl) {
                -offsetX + shakeOffset
            } else {
                offsetX + shakeOffset
            }

            val hintTransitionState = remember { MutableTransitionState(showTapHint) }.apply {
                targetState = showTapHint
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(visualX.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDrag = onDrag,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel
                        )
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                if (hintTransitionState.currentState || hintTransitionState.targetState) {
                    val yOffsetPx = remember(density) { with(density) { -48.dp.roundToPx() } }

                    Popup(
                        alignment = Alignment.TopCenter,
                        offset = IntOffset(0, yOffsetPx),
                        properties = PopupProperties(clippingEnabled = false)
                    ) {
                        AnimatedVisibility(
                            visibleState = hintTransitionState,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                tonalElevation = 4.dp
                            ) {
                                Text(
                                    text = stringResource(R.string.chat_input_hold_to_record),
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                HintTooltip(stringResource(R.string.chat_input_record_cd)) {
                    IconButton(
                        onClick = onShowTapHint
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_keyboard_voice_24),
                            contentDescription = stringResource(R.string.chat_input_record_cd),
                            tint = if (shakeOffset != 0f && !showTapHint) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
