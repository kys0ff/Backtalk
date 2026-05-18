package off.kys.backtalk.domain.use_case

import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case to cancel a scheduled message and its associated alarm.
 */
class CancelScheduledMessage(
    private val repository: MessagesRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(id: MessageId) {
        repository.deleteScheduledMessageById(id)
        alarmScheduler.cancel(id)
    }
}
