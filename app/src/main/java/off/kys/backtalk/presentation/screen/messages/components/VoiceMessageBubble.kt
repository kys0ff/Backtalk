package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.util.AudioPlayer
import org.koin.compose.koinInject
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun VoiceMessageBubble(
    voicePath: String,
    duration: Long,
    waveformData: List<Float>,
    contentColor: Color
) {
    val audioPlayer = koinInject<AudioPlayer>()
    val isPlayingGlobal by audioPlayer.isPlaying.collectAsState()
    val progressGlobal by audioPlayer.progress.collectAsState()
    val currentPath by audioPlayer.currentPath.collectAsState()

    val isThisPlaying = isPlayingGlobal && currentPath == voicePath
    val progress = if (currentPath == voicePath) progressGlobal else 0f

    val barWidth = 2.dp
    val gapWidth = 2.dp

    val rawWidth = (waveformData.size * (barWidth.value + gapWidth.value)).dp
    val dynamicWidth = rawWidth.coerceIn(40.dp, 200.dp)

    Row(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = {
                if (isThisPlaying) {
                    audioPlayer.pause()
                } else {
                    val file = File(voicePath)
                    if (file.exists()) {
                        if (currentPath == voicePath && progress > 0f && progress < 1f) {
                            audioPlayer.resume()
                        } else {
                            audioPlayer.playFile(file)
                        }
                    }
                }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (isThisPlaying) R.drawable.round_pause_24
                    else R.drawable.round_play_arrow_24
                ),
                contentDescription = null,
                tint = contentColor
            )
        }

        WaveformVisualizer(
            waveformData = waveformData,
            modifier = Modifier
                .height(32.dp)
                .width(dynamicWidth),
            color = contentColor,
            progress = progress,
            barWidth = barWidth,
            gapWidth = gapWidth
        )

        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun WaveformVisualizer(
    waveformData: List<Float>,
    modifier: Modifier = Modifier,
    color: Color,
    progress: Float = 1f,
    barWidth: Dp = 2.dp,
    gapWidth: Dp = 2.dp
) {
    val density = LocalDensity.current
    val barWidthPx = with(density) { barWidth.toPx() }
    val gapWidthPx = with(density) { gapWidth.toPx() }
    val totalBarWidthPx = barWidthPx + gapWidthPx

    Canvas(modifier = modifier) {
        val canvasHeight = size.height
        val canvasWidth = size.width

        val maxBarsThatFit = (canvasWidth / totalBarWidthPx).toInt()

        val barsToDraw = if (waveformData.size > maxBarsThatFit) {
            waveformData.takeLast(maxBarsThatFit)
        } else {
            waveformData
        }

        barsToDraw.forEachIndexed { index, amplitude ->
            val x = index * totalBarWidthPx
            val barHeight = (amplitude * canvasHeight).coerceAtLeast(2.dp.toPx())
            val y = (canvasHeight - barHeight) / 2

            val barProgress = index.toFloat() / (barsToDraw.size - 1).coerceAtLeast(1)
            val barColor = if (barProgress <= progress) color else color.copy(alpha = 0.3f)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return "%02d:%02d".format(minutes, seconds)
}