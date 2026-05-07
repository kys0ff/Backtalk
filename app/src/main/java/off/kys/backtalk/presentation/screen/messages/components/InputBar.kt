package off.kys.backtalk.presentation.screen.messages.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.util.AudioRecorder
import off.kys.backtalk.util.emptyString
import kotlin.math.roundToInt

private const val TAG = "InputBar"

@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    messageInput: String,
    replyingTo: MessageEntity?,
    editingMessage: MessageEntity?,
    onCancelReply: () -> Unit,
    onCancelEdit: () -> Unit,
    onMessageSend: (String) -> Unit,
    onVoiceSend: (String, Long, List<Float>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var showTapHint by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val amplitudes by audioRecorder.amplitudes.collectAsState()
    val shakeOffset = remember { Animatable(0f) }

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

    // Auto-dismiss the hint after 2 seconds of human confusion
    LaunchedEffect(showTapHint) {
        if (showTapHint) {
            delay(2000)
            showTapHint = false
        }
    }

    fun triggerDeniedShake() {
        scope.launch {
            repeat(4) {
                shakeOffset.animateTo(
                    targetValue = if (it % 2 == 0) 15f else -15f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioHighBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessHigh
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

    LaunchedEffect(key1 = messageInput) {
        if (messageInput != textValue.text) {
            textValue = TextFieldValue(text = messageInput, selection = TextRange(messageInput.length))
        }
    }

    fun applyStyle(startSym: String, endSym: String) {
        val selection = textValue.selection
        val text = textValue.text
        val selectedText = text.substring(selection.start, selection.end)
        val newText = text.replaceRange(selection.start, selection.end, "$startSym$selectedText$endSym")
        val newCursorPos = selection.start + startSym.length + selectedText.length + endSym.length
        textValue = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    }

    Surface(
        modifier = Modifier.imePadding(),
        tonalElevation = 2.dp
    ) {
        Column(modifier = modifier) {
            AnimatedVisibility(
                visible = replyingTo != null || editingMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(if (editingMessage != null) R.string.common_edit else R.string.chat_replying_to),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SmartText(
                            text = (editingMessage ?: replyingTo)?.text ?: emptyString(),
                            clickableLink = false,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(
                        onClick = if (editingMessage != null) onCancelEdit else onCancelReply,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_close_24),
                            contentDescription = stringResource(R.string.common_cancel)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
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
                            maxLines = 4,
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_keyboard_voice_24),
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            WaveformVisualizer(
                                waveformData = amplitudes,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "< Slide to cancel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                val showSend = textValue.text.isNotBlank() && !isRecording

                AnimatedContent(targetState = showSend, label = emptyString()) { targetShowSend ->
                    if (targetShowSend) {
                        IconButton(
                            onClick = {
                                if (textValue.text.isNotBlank()) {
                                    onMessageSend(textValue.text)
                                    textValue = TextFieldValue(emptyString())
                                }
                            }
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
                                        text = "Hold to record",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                                                    offsetX = (offsetX + dragAmount.x).coerceAtMost(0f)
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
                                    contentDescription = "Record",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormattingButton(label = "B") { applyStyle("**", "**") }
                    FormattingButton(label = "I") { applyStyle("*", "*") }
                    FormattingButton(label = "U") { applyStyle("__", "__") }
                    FormattingButton(label = "S") { applyStyle("~~", "~~") }
                    FormattingButton(label = "M") { applyStyle("`", "`") }
                }
            }
        }
    }
}

@Composable
private fun FormattingButton(
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}