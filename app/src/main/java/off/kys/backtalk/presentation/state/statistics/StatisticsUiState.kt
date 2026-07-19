package off.kys.backtalk.presentation.state.statistics

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import off.kys.backtalk.domain.model.MessageId
import java.time.LocalDate

@Immutable
data class StatisticsUiState(
    val totalMessages: Int = 0,
    val voiceMessagesCount: Int = 0,
    val textMessagesCount: Int = 0,
    val totalVoiceDurationMs: Long = 0,
    val scheduledMessagesCount: Int = 0,
    val activityLast7Days: PersistentList<DayActivity> = persistentListOf(),
    val heatmapData: PersistentList<HeatmapDay> = persistentListOf(),
    val topThreads: PersistentList<ThreadStat> = persistentListOf(),
    val avgMessageLength: Int = 0,
    val imageCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isLoading: Boolean = true
)

data class DayActivity(
    val dayName: String,
    val count: Int
)

data class HeatmapDay(
    val date: LocalDate,
    val count: Int
)

data class ThreadStat(
    val threadId: MessageId,
    val threadTitle: String,
    val messageCount: Int,
    val ratio: Float = 0f
)