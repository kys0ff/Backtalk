package off.kys.backtalk.domain.use_case

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.repository.MessagesRepository

class InsertMessage(
    private val repository: MessagesRepository
) {

    suspend operator fun invoke(messageEntity: MessageEntity) =
        repository.insertMessage(messageEntity)

}