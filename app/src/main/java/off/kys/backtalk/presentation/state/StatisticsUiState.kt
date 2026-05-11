package off.kys.backtalk.presentation.state

import androidx.compose.runtime.Immutable
import off.kys.backtalk.domain.model.MessageId

@Immutable
data class StatisticsUiState(
    val totalMessages: Int = 0,
    val voiceMessagesCount: Int = 0,
    val textMessagesCount: Int = 0,
    val totalVoiceDurationMs: Long = 0,
    val scheduledMessagesCount: Int = 0,
    val activityLast7Days: List<DayActivity> = emptyList(),
    val topThreads: List<ThreadStat> = emptyList(),
    val avgMessageLength: Int = 0,
    val isLoading: Boolean = true
)

data class DayActivity(
    val dayName: String,
    val count: Int
)

data class ThreadStat(
    val threadId: MessageId,
    val threadTitle: String,
    val messageCount: Int,
    val ratio: Float // relative to max thread
)