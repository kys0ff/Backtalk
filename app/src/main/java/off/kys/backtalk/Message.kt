package off.kys.backtalk

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: MessageId,
    val text: String,
    val timestamp: Long,
    @Embedded
    val repliedToId: MessageId?
)