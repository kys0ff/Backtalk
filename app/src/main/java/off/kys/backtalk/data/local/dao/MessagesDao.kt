package off.kys.backtalk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Data Access Object for the messages table.
 * Provides methods for performing CRUD operations on [MessageEntity].
 */
@Dao
interface MessagesDao {
    /**
     * Retrieves a message from the database by its unique identifier.
     *
     * @param id The [MessageId] of the message to retrieve.
     * @return The [MessageEntity] if found, or null if no message exists with the given [id].
     */
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessage(id: MessageId): MessageEntity?

    /**
     * Retrieves a list of messages from the database matching the provided set of identifiers.
     *
     * @param ids A set of [MessageId]s to retrieve.
     * @return A [Flow] emitting a list of [MessageEntity] matching the provided [ids].
     */
    @Query("SELECT * FROM messages WHERE id IN (:ids)")
    fun getMessagesByIds(ids: Set<MessageId>): Flow<List<MessageEntity>>

    /**
     * Inserts a message into the database.
     * If a message with the same identifier already exists, it will be replaced.
     *
     * @param messageEntity The [MessageEntity] to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    /**
     * Deletes a message from the database by its unique identifier.
     *
     * @param id The [MessageId] of the message to delete.
     */
    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: MessageId)

    /**
     * Retrieves all messages from the database.
     *
     * @return A [Flow] emitting a list of all [MessageEntity] present in the database.
     */
    @Query("SELECT * FROM messages")
    fun getAllMessages(): Flow<List<MessageEntity>>

    /**
     * Deletes all messages from the database.
     */
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    /**
     * Retrieves all messages from the database as a simple list.
     * Useful for statistics processing.
     */
    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesSync(): List<MessageEntity>

    /**
     * Updates the pinned status of a message.
     *
     * @param id The [MessageId] of the message to update.
     * @param isPinned The new pinned status.
     */
    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: MessageId, isPinned: Boolean)

    /**
     * Gets the timestamp of the last message sent that is not a reminder.
     */
    @Query("SELECT MAX(timestamp) FROM messages WHERE isReminder = 0")
    suspend fun getLastMessageTimestamp(): Long?

    /**
     * Counts how many messages use a specific media path.
     *
     * @param path The path to check.
     * @return The number of messages referencing this path.
     */
    @Query("SELECT COUNT(*) FROM messages WHERE voicePath = :path OR mediaPath = :path OR mediaPaths LIKE '%\"' || :path || '\"%'")
    suspend fun getPathUsageCount(path: String): Int
}
