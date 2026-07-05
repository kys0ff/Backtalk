package off.kys.backtalk.presentation.state.messages

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import off.kys.backtalk.presentation.model.MessageUiModel

@Immutable
data class MessageItemUiState(
    val message: MessageUiModel,
    val repliedMessage: MessageUiModel? = null,
    val isSelected: Boolean = false,
    val selectedImagePaths: PersistentSet<String> = persistentSetOf(),
    val showTimestamp: Boolean = false,
    val isTop: Boolean = false,
    val isBottom: Boolean = false,
    val isBlinking: Boolean = false,
    val showHint: Boolean = false,
)