package off.kys.backtalk.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import off.kys.backtalk.domain.model.MessageId

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: MessageId,
    val text: String,
    val timestamp: Long,
    @Embedded
    val repliedToId: MessageId?
)