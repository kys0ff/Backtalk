package off.kys.backtalk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.data.local.entity.ScheduledMessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Data Access Object for the scheduled_messages table.
 */
@Dao
interface ScheduledMessagesDao {
    /**
     * Retrieves a scheduled message by its ID.
     */
    @Query("SELECT * FROM scheduled_messages WHERE id = :id")
    suspend fun getScheduledMessage(id: MessageId): ScheduledMessageEntity?

    /**
     * Retrieves all scheduled messages.
     */
    @Query("SELECT * FROM scheduled_messages")
    fun getAllScheduledMessages(): Flow<List<ScheduledMessageEntity>>

    /**
     * Inserts a scheduled message.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMessage(scheduledMessage: ScheduledMessageEntity)

    /**
     * Deletes a scheduled message by its ID.
     */
    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteScheduledMessageById(id: MessageId)

    /**
     * Retrieves all scheduled messages as a list (non-flow).
     */
    @Query("SELECT * FROM scheduled_messages")
    suspend fun getAllScheduledMessagesSync(): List<ScheduledMessageEntity>

    /**
     * Counts how many scheduled messages use a specific media path.
     *
     * @param path The path to check.
     * @return The number of scheduled messages referencing this path.
     */
    @Query("SELECT COUNT(*) FROM scheduled_messages WHERE mediaPath = :path OR mediaPaths LIKE '%\"' || :path || '\"%'")
    suspend fun getPathUsageCount(path: String): Int
}
