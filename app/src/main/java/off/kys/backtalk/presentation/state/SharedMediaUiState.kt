package off.kys.backtalk.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class SharedMediaUiState(
    val media: List<MediaItemUiModel> = emptyList(),
    val voices: List<VoiceItemUiModel> = emptyList(),
    val links: List<LinkItemUiModel> = emptyList()
)