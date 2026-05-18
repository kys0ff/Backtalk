package off.kys.backtalk.domain.use_case

import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.data.local.entity.ScheduledMessageEntity
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case to retrieve all scheduled messages.
 */
class GetAllScheduledMessages(
    private val repository: MessagesRepository
) {
    operator fun invoke(): Flow<List<ScheduledMessageEntity>> {
        return repository.getAllScheduledMessages()
    }
}
