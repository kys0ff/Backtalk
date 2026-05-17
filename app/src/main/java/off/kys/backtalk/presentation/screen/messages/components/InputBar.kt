package off.kys.backtalk.presentation.screen.messages.components

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.util.AudioRecorder
import off.kys.backtalk.util.emptyString
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private const val TAG = "InputBar"

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    messageInput: String,
    replyingTo: MessageEntity?,
    editingMessage: MessageEntity?,
    onCancelReply: () -> Unit,
    onCancelEdit: () -> Unit,
    onMessageSend: (String) -> Unit,
    onVoiceSend: (String, Long, List<Float>) -> Unit,
    onMessageSchedule: (String, Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorder(context) }
    val haptic = LocalHapticFeedback.current
    val preferences = koinInject<BacktalkPreferences>()

    var isRecording by remember { mutableStateOf(false) }
    var showTapHint by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val amplitudes by audioRecorder.amplitudes.collectAsState()
    val shakeOffset = remember { Animatable(0f) }

    // --- Scheduling States ---
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i(TAG, "InputBar: Microphone permission is granted")
        }
    }

    var recordingStartTime by remember { mutableLongStateOf(0L) }
    var textValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = messageInput,
                selection = TextRange(messageInput.length)
            )
        )
    }

    val showPermissionRationale = remember { mutableStateOf(false) }

    fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                showPermissionRationale.value = true
            } else {
                showDatePicker.value = true
            }
        } else {
            showDatePicker.value = true
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndRequestExactAlarmPermission()
        }
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
                checkAndRequestExactAlarmPermission()
            }
        } else {
            checkAndRequestExactAlarmPermission()
        }
    }

    fun triggerDeniedShake() {
        scope.launch {
            repeat(4) {
                shakeOffset.animateTo(
                    targetValue = if (it % 2 == 0) 15f else -15f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
            }
            shakeOffset.animateTo(0f)
        }
    }

    fun startRecordingInternal() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            audioRecorder.startRecording()
        } else {
            triggerDeniedShake()
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun applyStyle(startSym: String, endSym: String) {
        val selection = textValue.selection
        val text = textValue.text
        val selectedText = text.substring(selection.start, selection.end)
        val newText =
            text.replaceRange(selection.start, selection.end, "$startSym$selectedText$endSym")
        val newCursorPos = selection.start + startSym.length + selectedText.length + endSym.length
        textValue = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    }

    LaunchedEffect(key1 = showTapHint) {
        if (showTapHint) {
            delay(2000)
            showTapHint = false
        }
    }

    LaunchedEffect(key1 = messageInput) {
        if (messageInput != textValue.text) {
            textValue =
                TextFieldValue(text = messageInput, selection = TextRange(messageInput.length))
        }
    }

    MessageSchedulingDialogs(
        showDatePicker = showDatePicker,
        showTimePicker = showTimePicker,
        datePickerState = datePickerState,
        timePickerState = timePickerState,
        onSchedule = { time ->
            onMessageSchedule(textValue.text, time)
            textValue = TextFieldValue(emptyString())
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
                replyingTo = replyingTo,
                editingMessage = editingMessage,
                onCancelReply = onCancelReply,
                onCancelEdit = onCancelEdit
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp) // now we have fixed issue #13 user cant see what he is typing because rge text field height was fixed on 56
                ) {
                    this@Row.AnimatedVisibility(
                        visible = !isRecording,
                        enter = fadeIn() + slideInHorizontally(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        TextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            textStyle = TextStyle(textDirection = TextDirection.Content),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                    }

                    this@Row.AnimatedVisibility(
                        visible = isRecording,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        VoiceRecordingIndicator(amplitudes = amplitudes)
                    }
                }

                val showSend = textValue.text.isNotBlank() && !isRecording

                AnimatedContent(targetState = showSend, label = emptyString()) { targetShowSend ->
                    if (targetShowSend) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (textValue.text.isNotBlank()) {
                                            onMessageSend(textValue.text)
                                            textValue = TextFieldValue(emptyString())
                                        }
                                    },
                                    onLongClick = {
                                        if (textValue.text.isNotBlank() && editingMessage == null) {
                                            if (preferences.hapticFeedbackEnabled) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                            handleScheduleClick()
                                        }
                                    },
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false)
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
                        Box(contentAlignment = Alignment.TopCenter) {
                            this@Row.AnimatedVisibility(
                                visible = showTapHint,
                                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                                modifier = Modifier.offset(y = (-48).dp)
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

                            IconButton(
                                onClick = {
                                    showTapHint = true
                                    triggerDeniedShake()
                                },
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (offsetX + shakeOffset.value).roundToInt(),
                                            0
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                showTapHint = false
                                                startRecordingInternal()
                                            },
                                            onDrag = { change, dragAmount ->
                                                if (isRecording) {
                                                    change.consume()
                                                    offsetX =
                                                        (offsetX + dragAmount.x).coerceAtMost(0f)
                                                    if (offsetX < -300f) {
                                                        isRecording = false
                                                        audioRecorder.cancelRecording()
                                                        offsetX = 0f
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                if (isRecording) {
                                                    isRecording = false
                                                    val file = audioRecorder.stopRecording()
                                                    if (file != null) {
                                                        onVoiceSend(
                                                            file.absolutePath,
                                                            System.currentTimeMillis() - recordingStartTime,
                                                            amplitudes
                                                        )
                                                    }
                                                }
                                                offsetX = 0f
                                            },
                                            onDragCancel = {
                                                if (isRecording) {
                                                    isRecording = false
                                                    audioRecorder.cancelRecording()
                                                }
                                                offsetX = 0f
                                            }
                                        )
                                    }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.round_keyboard_voice_24),
                                    contentDescription = stringResource(R.string.chat_input_record_cd),
                                    tint = if (shakeOffset.value != 0f && !showTapHint)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = textValue.selection.length > 0,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                FormattingToolbar(onFormattingClick = { start, end ->
                    applyStyle(start, end)
                })
            }
        }
    }

    if (showPermissionRationale.value) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationale.value = false },
            onConfirm = {
                showPermissionRationale.value = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
        )
    }
}