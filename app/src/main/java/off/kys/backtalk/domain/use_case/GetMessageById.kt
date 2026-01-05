package off.kys.backtalk.domain.use_case

import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository

class GetMessageById(
    private val repository: MessagesRepository
) {
    suspend operator fun invoke(id: MessageId) = repository.getMessageById(id)
}