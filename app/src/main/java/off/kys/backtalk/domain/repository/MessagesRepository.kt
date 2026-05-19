package off.kys.backtalk.domain.repository

import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.data.local.entity.ScheduledMessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Repository for managing [MessageEntity] data.
 */
interface MessagesRepository {

    /**
     * Returns a [Flow] of all messages in the repository.
     */
    fun getAllMessages(): Flow<List<MessageEntity>>

    /**
     * Retrieves a message by its [id].
     *
     * @param id The ID of the message to retrieve.
     * @return The message with the given ID, or null if not found.
     */
    suspend fun getMessageById(id: MessageId): MessageEntity?

    /**
     * Returns a [Flow] of messages with the given [ids].
     *
     * @param ids The set of IDs of the messages to retrieve.
     */
    fun getMessagesByIds(ids: Set<MessageId>): Flow<List<MessageEntity>>

    /**
     * Inserts a [messageEntity] into the repository.
     *
     * @param messageEntity The message to insert.
     */
    suspend fun insertMessage(messageEntity: MessageEntity)

    /**
     * Deletes a message by its [id].
     *
     * @param id The ID of the message to delete.
     */
    suspend fun deleteMessageById(id: MessageId)

    /**
     * Inserts a [scheduledMessageEntity] into the repository.
     */
    suspend fun insertScheduledMessage(scheduledMessageEntity: ScheduledMessageEntity)

    /**
     * Returns a [Flow] of all scheduled messages.
     */
    fun getAllScheduledMessages(): Flow<List<ScheduledMessageEntity>>

    /**
     * Retrieves a scheduled message by its [id].
     */
    suspend fun getScheduledMessageById(id: MessageId): ScheduledMessageEntity?

    /**
     * Deletes a scheduled message by its [id].
     */
    suspend fun deleteScheduledMessageById(id: MessageId)

    /**
     * Retrieves all scheduled messages as a list.
     */
    suspend fun getAllScheduledMessagesSync(): List<ScheduledMessageEntity>

    /**
     * Retrieves all messages as a list.
     */
    suspend fun getAllMessagesSync(): List<MessageEntity>

    /**
     * Updates the pinned status of a message.
     *
     * @param id The ID of the message to update.
     * @param isPinned The new pinned status.
     */
    suspend fun updatePinnedStatus(id: MessageId, isPinned: Boolean)

    /**
     * Checks if a media path is referenced by any message or scheduled message.
     *
     * @param path The path to check.
     * @return True if the path is referenced, false otherwise.
     */
    suspend fun isPathReferenced(path: String): Boolean
}