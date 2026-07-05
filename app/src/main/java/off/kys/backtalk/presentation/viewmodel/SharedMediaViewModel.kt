package off.kys.backtalk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import off.kys.backtalk.domain.use_case.GetAllMessages
import off.kys.backtalk.presentation.event.SharedMediaUiEvent
import off.kys.backtalk.presentation.state.messages.LinkItemUiModel
import off.kys.backtalk.presentation.state.messages.MediaItemUiModel
import off.kys.backtalk.presentation.state.messages.SharedMediaUiState
import off.kys.backtalk.presentation.state.messages.VoiceItemUiModel
import off.kys.backtalk.util.AudioPlayer
import off.kys.backtalk.util.ComposeTextParser
import java.io.File

import kotlinx.collections.immutable.toPersistentList

/**
 * ViewModel for the Shared Media screen/component.
 *
 * This ViewModel handles the logic for extracting and displaying shared media,
 * voice messages, and links from the message history.
 */
class SharedMediaViewModel(
    getAllMessages: GetAllMessages,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    /**
     * The UI state for shared media, derived from messages and audio player state.
     */
    val uiState: StateFlow<SharedMediaUiState> = combine(
        getAllMessages(),
        audioPlayer.isPlaying,
        audioPlayer.progress,
        audioPlayer.currentPath
    ) { messages, isPlaying, progress, audioPath ->
        val media = messages.flatMap { message ->
            message.mediaPaths.orEmpty().map { path ->
                MediaItemUiModel(id = message.id, path = path)
            }
        }.reversed()

        val voices = messages.filter { it.voicePath != null }.map { message ->
            val path = message.voicePath!!
            VoiceItemUiModel(
                id = message.id,
                path = path,
                duration = message.voiceDuration ?: 0L,
                waveformData = message.waveformData?.toPersistentList() ?: emptyList<Float>().toPersistentList(),
                isPlaying = isPlaying && (audioPath == path),
                progress = if (audioPath == path) progress else 0f
            )
        }.reversed()

        val links = messages.flatMap { message ->
            val text = message.editedText ?: message.text
            val nakedUrls = ComposeTextParser.NAKED_URL_REGEX.findAll(text).map { it.value }
            val markdownUrls = ComposeTextParser.MARKDOWN_LINK_REGEX.findAll(text).map { it.groupValues[2] }
            (nakedUrls + markdownUrls).map { url ->
                LinkItemUiModel(id = message.id, url = url)
            }
        }.reversed()

        SharedMediaUiState(media = media, voices = voices, links = links)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharedMediaUiState()
    )

    /**
     * Handles incoming UI events.
     *
     * @param event The event to handle.
     */
    fun onEvent(event: SharedMediaUiEvent) = when (event) {
        is SharedMediaUiEvent.ToggleVoicePlay -> toggleVoicePlay(event.voiceItem)
    }

    /**
     * Toggles playback of a voice message.
     *
     * @param voiceItem The voice message item to toggle.
     */
    private fun toggleVoicePlay(voiceItem: VoiceItemUiModel) {
        val currentPath = audioPlayer.currentPath.value
        val isPlaying = audioPlayer.isPlaying.value

        if (isPlaying && currentPath == voiceItem.path) {
            audioPlayer.pause()
        } else {
            if (currentPath == voiceItem.path) {
                audioPlayer.resume()
            } else {
                audioPlayer.playFile(File(voiceItem.path))
            }
        }
    }
}
