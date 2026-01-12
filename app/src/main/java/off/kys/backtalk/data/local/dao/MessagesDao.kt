package off.kys.backtalk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessage(id: MessageId): MessageEntity?

    @Query("SELECT * FROM messages WHERE id IN (:ids)")
    fun getMessagesByIds(ids: Set<MessageId>): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: MessageId)

    @Query("SELECT * FROM messages")
    fun getAllMessages(): Flow<List<MessageEntity>>
}