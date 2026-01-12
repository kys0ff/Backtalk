package off.kys.backtalk.domain.repository

import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

interface MessagesRepository {

    fun getAllMessages(): Flow<List<MessageEntity>>

    suspend fun getMessageById(id: MessageId): MessageEntity?

    fun getMessagesByIds(ids: Set<MessageId>): Flow<List<MessageEntity>>

    suspend fun insertMessage(messageEntity: MessageEntity)

    suspend fun deleteMessageById(id: MessageId)
}