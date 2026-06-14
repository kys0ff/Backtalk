package off.kys.backtalk.presentation.event

import off.kys.backtalk.presentation.state.VoiceItemUiModel

/**
 * Sealed interface representing UI events for the shared media screen.
 */
sealed interface SharedMediaUiEvent {
    /**
     * UI event to toggle playback of a voice message.
     *
     * @param voiceItem The voice message item to toggle.
     */
    data class ToggleVoicePlay(val voiceItem: VoiceItemUiModel) : SharedMediaUiEvent
}
