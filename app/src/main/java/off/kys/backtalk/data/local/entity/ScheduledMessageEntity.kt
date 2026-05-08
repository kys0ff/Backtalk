package off.kys.backtalk.data.local.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import off.kys.backtalk.domain.model.MessageId

/**
 * Represents a message that is scheduled to be sent in the future.
 *
 * @property id The unique identifier of the scheduled message.
 * @property text The body content of the message.
 * @property creationTimestamp The time the message was originally typed and scheduled.
 * @property scheduledTimestamp The time the message is intended to be delivered.
 * @property repliedToId The ID of the message this message is replying to, or null.
 */
@Keep
@Serializable
@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey
    val id: MessageId,
    val text: String,
    val creationTimestamp: Long,
    val scheduledTimestamp: Long,
    @Embedded
    val repliedToId: MessageId? = null
)
