package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.event.InputBarEvent
import off.kys.backtalk.presentation.model.MessageUiModel
import off.kys.backtalk.presentation.state.InputBarEffect
import off.kys.backtalk.presentation.status.SchedulingStage
import off.kys.backtalk.presentation.viewmodel.InputBarViewModel
import off.kys.backtalk.util.getFirstLinkOrNull
import off.kys.backtalk.util.toast
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    viewModel: InputBarViewModel,
    messageInput: String,
    replyingTo: MessageUiModel?,
    editingMessage: MessageUiModel?,
    sharedImageUris: List<String> = emptyList(),
    onCancelSharedImage: () -> Unit = {},
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val preferences = koinInject<BacktalkPreferences>()
    val layoutDirection = LocalLayoutDirection.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val onCancelReply = remember(viewModel) { { viewModel.onEvent(InputBarEvent.CancelReply) } }
    val onCancelEdit = remember(viewModel) { { viewModel.onEvent(InputBarEvent.CancelEdit) } }

    val shakeOffset = remember { Animatable(0f) }

    val datePickerState = rememberDatePickerState(
        selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Grab today's local date, then determine what UTC midnight matches it
                    val localToday = LocalDate.now(ZoneId.systemDefault())
                    val todayUtcMillis =
                        localToday.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                    return utcTimeMillis >= todayUtcMillis
                }
            }
        }
    )

    val calendar = Calendar.getInstance()
    val isSystem24HourFormat =
        remember(preferences, context) { preferences.timeFormat.is24Hour(context) }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = isSystem24HourFormat
    )

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    val isImeVisible = imeHeight > 0

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                InputBarEffect.TriggerShake -> {
                    repeat(4) {
                        shakeOffset.animateTo(10f, tween(40))
                        shakeOffset.animateTo(-10f, tween(40))
                    }
                    shakeOffset.animateTo(0f, tween(40))
                }

                is InputBarEffect.ShowError -> {
                    context.toast(effect.messageRes)
                }
            }
        }
    }

    LaunchedEffect(messageInput) {
        if (messageInput.isNotEmpty()) {
            state.textFieldState.setTextAndPlaceCursorAtEnd(messageInput)
        } else {
            state.textFieldState.clearText()
        }
    }

    LaunchedEffect(replyingTo, editingMessage) {
        viewModel.onEvent(InputBarEvent.UpdateReplyingTo(replyingTo))
        viewModel.onEvent(InputBarEvent.UpdateEditingMessage(editingMessage))
    }

    fun handleScheduleClick() {
        if (state.textFieldState.text.isNotBlank()) {
            if (preferences.hapticFeedbackEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            viewModel.onEvent(InputBarEvent.ChangeSchedulingStage(SchedulingStage.SelectingDate))
        } else {
            scope.launch {
                repeat(4) {
                    shakeOffset.animateTo(10f, tween(40))
                    shakeOffset.animateTo(-10f, tween(40))
                }
                shakeOffset.animateTo(0f, tween(40))
            }
        }
    }

    fun startRecordingInternal() {
        if (preferences.hapticFeedbackEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        viewModel.onEvent(InputBarEvent.StartRecording)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp + with(density) { imeHeight.toDp() })
            .let { if (isImeVisible) it else it.navigationBarsPadding() }
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        tonalElevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            InputBarReplyHeader(
                replyingTo = state.replyingTo,
                editingMessage = state.editingMessage,
                onCancelReply = onCancelReply,
                onCancelEdit = onCancelEdit
            )

            if (sharedImageUris.isNotEmpty()) {
                SharedImageHeader(
                    uris = sharedImageUris,
                    onCancel = onCancelSharedImage
                )
            }

            LinkPreviewSection(
                text = state.textFieldState.text.toString(),
                enabled = state.linkPreviewEnabled
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                AttachButtonVisibility(
                    isVisible = !state.isRecording,
                    onClick = { viewModel.onEvent(InputBarEvent.AttachClicked) }
                )

                ChatTextField(
                    modifier = Modifier.weight(1f),
                    textFieldState = state.textFieldState,
                    isRecording = state.isRecording,
                    amplitudes = state.amplitudes,
                    durationText = state.durationText,
                    sendWithEnter = remember(preferences) { preferences.sendWithEnter },
                    onSend = { viewModel.onEvent(InputBarEvent.SendMessage(state.textFieldState.text.toString())) },
                    onContentReceived = { viewModel.onEvent(InputBarEvent.ContentReceived(it)) }
                )

                ActionButtons(
                    isRecording = state.isRecording,
                    onSendMessage = { viewModel.onEvent(InputBarEvent.SendMessage(state.textFieldState.text.toString())) },
                    onStartRecording = ::startRecordingInternal,
                    isSendButtonVisible = state.isSendButtonVisible,
                    maxDragX = with(density) { 110.dp.toPx() },
                    onCancelRecording = { viewModel.onEvent(InputBarEvent.CancelRecording) },
                    onStopAndSendRecording = { viewModel.onEvent(InputBarEvent.StopAndSendRecording) },
                    onDragUpdate = { directedX ->
                        viewModel.onEvent(InputBarEvent.UpdateOffsetX(directedX))
                    },
                    onLongClick = ::handleScheduleClick,
                    onShowTapHint = { viewModel.onEvent(InputBarEvent.ShowTapHint) },
                    layoutDirection = layoutDirection
                )
            }
        }
    }

    if (state.schedulingStage == SchedulingStage.SelectingDate) {
        DatePickerDialog(
            onDismissRequest = {
                viewModel.onEvent(InputBarEvent.ChangeSchedulingStage(SchedulingStage.Hidden))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(InputBarEvent.ChangeSchedulingStage(SchedulingStage.SelectingTime))
                }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onEvent(InputBarEvent.ChangeSchedulingStage(SchedulingStage.Hidden))
                }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (state.schedulingStage == SchedulingStage.SelectingTime) {
        TimePickerDialog(
            onDismissRequest = {
                viewModel.onEvent(InputBarEvent.ChangeSchedulingStage(SchedulingStage.Hidden))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedUtcMillis =
                            datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        val date = Instant.ofEpochMilli(selectedUtcMillis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val scheduledDateTime = LocalDateTime.of(date, time)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        viewModel.onEvent(
                            InputBarEvent.ScheduleMessage(
                                state.textFieldState.text.toString(),
                                scheduledDateTime
                            )
                        )
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun LinkPreviewSection(text: String, enabled: Boolean) {
    val firstUrl = remember(text) { text.getFirstLinkOrNull() }
    AnimatedVisibility(
        visible = enabled && firstUrl != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        if (firstUrl != null) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                LinkPreviewCard(url = firstUrl)
            }
        }
    }
}

@Composable
private fun AttachButtonVisibility(isVisible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.round_image_24),
                contentDescription = stringResource(R.string.common_attach),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    isRecording: Boolean,
    amplitudes: List<Float>,
    durationText: String,
    sendWithEnter: Boolean,
    onSend: () -> Unit,
    onContentReceived: (TransferableContent) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.2f else 0.8f,
        label = "TextFieldBackgroundAlpha"
    )

    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = backgroundAlpha))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (isRecording) {
            VoiceRecordingIndicator(
                amplitudes = amplitudes,
                durationText = durationText,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            BasicTextField(
                state = textFieldState,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .contentReceiver {
                        onContentReceived(it)
                        it
                    }
                    .onKeyEvent {
                        if (it.key == Key.Enter && it.isCtrlPressed) {
                            onSend()
                            true
                        } else {
                            false
                        }
                    },
                lineLimits = if (sendWithEnter) TextFieldLineLimits.SingleLine else TextFieldLineLimits.MultiLine(
                    1,
                    5
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(
                    imeAction = if (sendWithEnter) ImeAction.Send else ImeAction.Default,
                    keyboardType = KeyboardType.Text
                ),
                onKeyboardAction = { onSend() },
                decorator = { innerTextField ->
                    if (textFieldState.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_input_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isRecording: Boolean,
    onSendMessage: () -> Unit,
    onStartRecording: () -> Unit,
    isSendButtonVisible: Boolean,
    maxDragX: Float,
    onCancelRecording: () -> Unit,
    onStopAndSendRecording: () -> Unit,
    onDragUpdate: (Float) -> Unit,
    onLongClick: () -> Unit,
    onShowTapHint: () -> Unit,
    layoutDirection: LayoutDirection
) {
    val interactionSource = remember { MutableInteractionSource() }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSendButtonVisible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false),
                        onClick = onSendMessage,
                        onLongClick = onLongClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_send_24),
                    contentDescription = stringResource(R.string.common_send),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = !isSendButtonVisible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            val recordButtonColor by animateColorAsState(
                if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                label = "RecordButtonColor"
            )

            Box(
                modifier = Modifier
                    .size(if (isRecording) 44.dp else 40.dp)
                    .clip(CircleShape)
                    .background(recordButtonColor)
                    .pointerInput(layoutDirection) {
                        detectDragGestures(
                            onDragStart = {
                                dragAccumulator = 0f
                                onStartRecording()
                            },
                            onDragEnd = {
                                if (abs(dragAccumulator) < maxDragX) {
                                    onStopAndSendRecording()
                                } else {
                                    onCancelRecording()
                                }
                                dragAccumulator = 0f
                                onDragUpdate(0f)
                            },
                            onDragCancel = {
                                onCancelRecording()
                                dragAccumulator = 0f
                                onDragUpdate(0f)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()

                                val delta = dragAmount.x
                                dragAccumulator += delta

                                val directedX =
                                    if (layoutDirection == LayoutDirection.Rtl) -dragAccumulator else dragAccumulator
                                onDragUpdate(directedX)

                                if (abs(dragAccumulator) >= maxDragX) {
                                    onCancelRecording()
                                }
                            }
                        )
                    }
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onShowTapHint,
                        onLongClick = onLongClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_keyboard_voice_24),
                    contentDescription = stringResource(R.string.chat_input_record_cd),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SharedImageHeader(uris: List<String>, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.round_image_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(R.string.chat_input_send_images, uris.size),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        )
        IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
            Icon(
                painter = painterResource(R.drawable.round_close_24),
                contentDescription = stringResource(R.string.common_cancel),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(shape = MaterialTheme.shapes.extraLarge, color = containerColor),
            color = containerColor
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}