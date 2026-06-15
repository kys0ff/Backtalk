package off.kys.backtalk.presentation.state

import androidx.compose.runtime.Immutable
import off.kys.backtalk.domain.model.MessageId

@Immutable
data class MediaItemUiModel(
    val id: MessageId,
    val path: String
)