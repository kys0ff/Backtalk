package off.kys.backtalk.data.repository

import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Implementation of the [MessagesRepository] interface that uses a [MessagesDao] to interact with the database.
 * @param messagesDao The [MessagesDao] used to access the database.
 */
class MessagesRepositoryImpl(
    private val messagesDao: MessagesDao
) : MessagesRepository {

    /**
     * Get all messages from the database.
     * @return Flow of a list of [MessageEntity] objects.
     */
    override fun getAllMessages(): Flow<List<MessageEntity>> = messagesDao.getAllMessages()

    /**
     * Get a specific message from the database by its ID.
     * @param id The ID of the message to retrieve.
     */
    override suspend fun getMessageById(id: MessageId): MessageEntity? = messagesDao.getMessage(id)

    /**
     * Insert a new message into the database.
     * @param messageEntity The [MessageEntity] object to insert.
     */
    override suspend fun insertMessage(messageEntity: MessageEntity) = messagesDao.insertMessage(messageEntity)

    /**
     * Delete a message from the database by its ID.
     * @param id The ID of the message to delete.
     */
    override suspend fun deleteMessageById(id: MessageId) = messagesDao.deleteMessageById(id)
}