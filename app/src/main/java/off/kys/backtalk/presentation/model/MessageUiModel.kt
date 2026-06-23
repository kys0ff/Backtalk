package off.kys.backtalk.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import off.kys.backtalk.domain.model.MessageId

@Immutable
data class MessageUiModel(
    val id: MessageId,
    val text: String,
    val timestamp: Long,
    val repliedToId: MessageId?,
    val editedText: String?,
    val editedAt: Long?,
    val voicePath: String?,
    val voiceDuration: Long?,
    val waveformData: PersistentList<Float>?,
    val isReminder: Boolean,
    val originalCreationTimestamp: Long?,
    val scheduledTimestamp: Long?,
    val isPinned: Boolean,
    val mediaPath: String?,
    val mediaPaths: PersistentList<String>?,
    val mediaType: String?,
    
    // UI pre-calculated flags
    val isDefaultCaption: Boolean,
    val isLocked: Boolean,
    val canEdit: Boolean,
    val hasImages: Boolean,
    val hasVoice: Boolean,
    val hasText: Boolean,
    val hasRepliedMessage: Boolean,
    val hasTags: Boolean,
    val isImageOnly: Boolean,
    val visibleText: String
)
