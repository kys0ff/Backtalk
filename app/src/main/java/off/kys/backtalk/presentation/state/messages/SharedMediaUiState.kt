package off.kys.backtalk.presentation.state.messages

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class SharedMediaUiState(
    val media: PersistentList<MediaItemUiModel> = persistentListOf(),
    val voices: PersistentList<VoiceItemUiModel> = persistentListOf(),
    val links: PersistentList<LinkItemUiModel> = persistentListOf()
)
