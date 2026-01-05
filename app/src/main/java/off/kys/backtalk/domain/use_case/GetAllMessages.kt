package off.kys.backtalk.domain.use_case

import off.kys.backtalk.domain.repository.MessagesRepository

class GetAllMessages(
    private val repository: MessagesRepository
) {
    operator fun invoke() = repository.getAllMessages()
}