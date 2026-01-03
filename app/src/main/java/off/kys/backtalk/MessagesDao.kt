package off.kys.backtalk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessage(id: MessageId): Message?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: MessageId)

    @Query("SELECT * FROM messages")
    fun getAllMessages(): Flow<List<Message>>
}