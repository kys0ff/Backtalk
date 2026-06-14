package off.kys.backtalk.presentation.state

import androidx.compose.runtime.Immutable
import off.kys.backtalk.domain.model.MessageId

@Immutable
data class SharedMediaUiState(
    val media: List<MediaItemUiModel> = emptyList(),
    val voices: List<VoiceItemUiModel> = emptyList(),
    val links: List<LinkItemUiModel> = emptyList()
)

@Immutable
data class MediaItemUiModel(
    val id: MessageId,
    val path: String
)

@Immutable
data class VoiceItemUiModel(
    val id: MessageId,
    val path: String,
    val duration: Long,
    val waveformData: List<Float>,
    val isPlaying: Boolean,
    val progress: Float
)

@Immutable
data class LinkItemUiModel(
    val id: MessageId,
    val url: String
)
