package off.kys.backtalk.domain.use_case

import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.data.local.entity.ScheduledMessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case to schedule a message for delivery at a future time.
 */
class ScheduleMessageUseCase(
    private val repository: MessagesRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(text: String, scheduledTime: Long, repliedToId: MessageId? = null) {
        val scheduledMessage = ScheduledMessageEntity(
            id = MessageId.generate(),
            text = text,
            creationTimestamp = System.currentTimeMillis(),
            scheduledTimestamp = scheduledTime,
            repliedToId = repliedToId
        )
        
        repository.insertScheduledMessage(scheduledMessage)
        alarmScheduler.schedule(scheduledMessage)
    }
}
