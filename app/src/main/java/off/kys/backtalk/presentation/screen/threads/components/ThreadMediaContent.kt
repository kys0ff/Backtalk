package off.kys.backtalk.presentation.screen.threads.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.collections.immutable.toPersistentList
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.presentation.screen.messages.components.StaggeredImageGrid
import off.kys.backtalk.presentation.screen.messages.components.VoiceMessageBubbleContent
import off.kys.backtalk.presentation.screen.preview.ImagePreviewScreen
import off.kys.backtalk.util.AudioPlayer
import org.koin.compose.koinInject
import java.io.File

@Composable
fun ThreadMediaContent(
    message: MessageEntity,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Unspecified
) {
    val navigator = LocalNavigator.current
    val preferences = koinInject<BacktalkPreferences>()
    val audioPlayer = koinInject<AudioPlayer>()

    val images = remember(message) {
        val list = mutableListOf<String>()
        message.mediaPath?.let { list.add(it) }
        message.mediaPaths?.let { list.addAll(it) }
        list
    }

    val hasImages = images.isNotEmpty()
    val hasVoice = message.voicePath != null

    if (!hasImages && !hasVoice) return

    Column(modifier = modifier) {
        if (hasImages) {
            Spacer(modifier = Modifier.height(8.dp))
            StaggeredImageGrid(
                images = images,
                isGif = message.mediaType == "image/gif",
                onImageClick = { imagePath ->
                    navigator?.push(ImagePreviewScreen(imagePath))
                },
                hapticFeedbackEnabled = preferences.hapticFeedbackEnabled
            )
        }

        if (hasVoice) {
            Spacer(modifier = Modifier.height(8.dp))
            val isPlayingState by audioPlayer.isPlaying.collectAsStateWithLifecycle()
            val progressState by audioPlayer.progress.collectAsStateWithLifecycle()
            val currentPathState by audioPlayer.currentPath.collectAsStateWithLifecycle()

            val isThisPlaying = isPlayingState && currentPathState == message.voicePath

            VoiceMessageBubbleContent(
                duration = message.voiceDuration ?: 0L,
                waveformData = message.waveformData?.toPersistentList() ?: emptyList<Float>().toPersistentList(),
                contentColor = if (contentColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else contentColor,
                isPlaying = isThisPlaying,
                progress = if (isThisPlaying) progressState else 0f,
                onTogglePlay = {
                    if (isThisPlaying) {
                        audioPlayer.pause()
                    } else {
                        if (currentPathState == message.voicePath) {
                            audioPlayer.resume()
                        } else {
                            audioPlayer.playFile(File(message.voicePath))
                        }
                    }
                }
            )
        }
    }
}
