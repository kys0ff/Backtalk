package off.kys.backtalk.data.local.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
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
 * @property threadId The ID of the root message of the thread this message will join once delivered,
 * or null to make it a new thread root.
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
    val repliedToId: MessageId? = null,
    @ColumnInfo(name = "threadId")
    val threadId: MessageId? = null,
    val mediaPath: String? = null,
    val mediaPaths: List<String>? = null,
    val mediaType: String? = null
) : java.io.Serializable

