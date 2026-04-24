package off.kys.backtalk.domain.use_case

import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case to retrieve all messages.
 */
class GetAllMessages(
    private val repository: MessagesRepository
) {
    /**
     * Executes the use case and returns a flow of all messages.
     */
    operator fun invoke() = repository.getAllMessages()
}