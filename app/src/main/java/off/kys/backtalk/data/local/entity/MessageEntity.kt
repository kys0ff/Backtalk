package off.kys.backtalk.data.local.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import off.kys.backtalk.domain.model.MessageId

/**
 * Represents a message entity stored in the local Room database.
 *
 * @property id The unique identifier of the message.
 * @property text The body content of the message.
 * @property timestamp The time the message was sent, in milliseconds.
 * @property repliedToId The ID of the message this message is replying to, or null if it's not a reply.
 * @property editedText The content of the message after being edited, or null if it hasn't been edited.
 * @property editedAt The time the message was last edited, or null if it hasn't been edited.
 * @property voicePath The local path to the voice message audio file, if applicable.
 * @property voiceDuration The duration of the voice message in milliseconds, if applicable.
 * @property waveformData A list of floats representing the audio waveform, if applicable.
 */
@Keep
@Serializable
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: MessageId,
    val text: String,
    val timestamp: Long,
    @Embedded
    val repliedToId: MessageId?,
    val editedText: String? = null,
    val editedAt: Long? = null,
    val voicePath: String? = null,
    val voiceDuration: Long? = null,
    val waveformData: List<Float>? = null,
    val isReminder: Boolean = false,
    val originalCreationTimestamp: Long? = null,
    val scheduledTimestamp: Long? = null,
    val isPinned: Boolean = false,
    val mediaPath: String? = null,
    val mediaPaths: List<String>? = null,
    val mediaType: String? = null
)
