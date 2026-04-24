package off.kys.backtalk.domain.use_case

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case for inserting a message into the database.
 *
 * @property repository The [MessagesRepository] used to interact with message data.
 */
class InsertMessage(
    private val repository: MessagesRepository
) {

    /**
     * Executes the use case to insert a [MessageEntity].
     *
     * @param messageEntity The message to be inserted.
     */
    suspend operator fun invoke(messageEntity: MessageEntity) =
        repository.insertMessage(messageEntity)

}