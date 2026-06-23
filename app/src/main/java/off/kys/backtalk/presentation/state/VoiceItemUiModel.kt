package off.kys.backtalk.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import off.kys.backtalk.domain.model.MessageId

@Immutable
data class VoiceItemUiModel(
    val id: MessageId,
    val path: String,
    val duration: Long,
    val waveformData: PersistentList<Float>,
    val isPlaying: Boolean,
    val progress: Float
)
