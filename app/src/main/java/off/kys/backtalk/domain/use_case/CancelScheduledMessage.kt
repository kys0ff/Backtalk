package off.kys.backtalk.domain.use_case

import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository
import java.io.File

/**
 * Use case to cancel a scheduled message and its associated alarm.
 */
class CancelScheduledMessage(
    private val repository: MessagesRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(id: MessageId) {
        val scheduledMessage = repository.getScheduledMessageById(id) ?: return
        repository.deleteScheduledMessageById(id)
        alarmScheduler.cancel(id)

        val paths = mutableListOf<String>()
        scheduledMessage.mediaPath?.let { paths.add(it) }
        scheduledMessage.mediaPaths?.let { paths.addAll(it) }

        paths.forEach { path ->
            if (!repository.isPathReferenced(path)) {
                File(path).let { file ->
                    if (file.exists()) file.delete()
                }
            }
        }
    }
}
