package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import off.kys.backtalk.R
import off.kys.backtalk.presentation.state.messages.VoicePreviewData
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun VoicePreviewDialog(
    previewData: VoicePreviewData,
    isPlaying: Boolean,
    progress: Float,
    captionState: TextFieldState,
    onTogglePlay: () -> Unit,
    onCancel: () -> Unit,
    onSend: (String) -> Unit,
    onSeek: (Float) -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    // Guards against a double tap firing onSend twice while the dialog is closing.
    var isSending by remember { mutableStateOf(false) }

    fun performSend() {
        if (isSending) return
        isSending = true
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onSend(captionState.text.toString())
    }

    // Small entrance animation so the dialog doesn't just snap into place.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        focusRequester.requestFocus()
    }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dialogScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dialogAlpha"
    )

    val elapsedMs = (previewData.duration * progress.coerceIn(0f, 1f)).toLong()

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = contentAlpha
                }
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: title + explicit close affordance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.chat_voice_message),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter=painterResource(R.drawable.round_close_24),
                            contentDescription = "Discard recording",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                // Waveform and playback controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePlay()
                        },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        // Crossfade + scale between play/pause instead of a hard swap
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                            },
                            label = "playPauseIcon"
                        ) { playing ->
                            Icon(
                                painter = painterResource(
                                    if (playing) R.drawable.round_pause_24
                                    else R.drawable.round_play_arrow_24
                                ),
                                contentDescription = if (playing) "Pause" else "Play"
                            )
                        }
                    }

                    // Waveform is now tap-to-seek, not just a static visual
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                    onSeek(fraction)
                                }
                            }
                    ) {
                        WaveformVisualizer(
                            waveformData = previewData.amplitudes,
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            progress = progress
                        )
                    }

                    // Elapsed / total instead of a fixed duration, so it reads as "live"
                    Text(
                        text = "${formatDuration(elapsedMs)} / ${formatDuration(previewData.duration)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                // Caption input now autofocused so the keyboard is ready immediately
                BasicTextField(
                    state = captionState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Text
                    ),
                    onKeyboardAction = { performSend() },
                    decorator = { innerTextField ->
                        if (captionState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.chat_input_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.size(20.dp))

                // Single, prominent send action — cancel already lives in the header as the "X"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledIconButton(
                        onClick = { performSend() },
                        enabled = !isSending,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_send_24),
                            contentDescription = stringResource(R.string.common_send)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}