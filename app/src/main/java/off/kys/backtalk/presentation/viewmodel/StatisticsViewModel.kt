package off.kys.backtalk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.presentation.state.statistics.DayActivity
import off.kys.backtalk.presentation.state.statistics.HeatmapDay
import off.kys.backtalk.presentation.state.statistics.StatisticsUiState
import off.kys.backtalk.presentation.state.statistics.ThreadStat
import off.kys.backtalk.util.emptyString
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale

class StatisticsViewModel(
    private val repository: MessagesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsUiState())
    val state = _state.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val allMessages = repository.getAllMessagesSync()
            val scheduledMessages = repository.getAllScheduledMessagesSync()

            withContext(Dispatchers.Default) {
                val voiceMessages = allMessages.filter { it.voicePath != null }
                val textMessages =
                    allMessages.filter { it.voicePath == null && it.mediaPaths == null && it.mediaPath == null }

                val mediaMessages =
                    allMessages.filter { it.mediaPaths != null || it.mediaPath != null }
                val imageCount =
                    mediaMessages.filter { it.mediaType?.contains("image") == true || it.mediaType == null }
                        .sumOf { (it.mediaPaths?.size ?: 0) + (if (it.mediaPath != null) 1 else 0) }

                val totalVoiceDuration = voiceMessages.sumOf { it.voiceDuration ?: 0L }
                val avgLen = if (textMessages.isNotEmpty()) {
                    textMessages.sumOf { it.text.length } / textMessages.size
                } else 0

                val activity = calculateLast7DaysActivity(allMessages).toPersistentList()
                val heatmapData = calculateHeatmapData(allMessages).toPersistentList()
                val currentStreak = calculateCurrentStreak(allMessages)

                val topThreads = calculateTopThreads(allMessages).toPersistentList()

                _state.update {
                    it.copy(
                        totalMessages = allMessages.size,
                        voiceMessagesCount = voiceMessages.size,
                        textMessagesCount = textMessages.size,
                        totalVoiceDurationMs = totalVoiceDuration,
                        scheduledMessagesCount = scheduledMessages.size,
                        activityLast7Days = activity,
                        heatmapData = heatmapData,
                        topThreads = topThreads,
                        avgMessageLength = avgLen,
                        imageCount = imageCount,
                        currentStreak = currentStreak,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateCurrentStreak(messages: List<MessageEntity>): Int {
        if (messages.isEmpty()) return 0

        val messageDays = messages.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - 24 * 60 * 60 * 1000

        if (messageDays.isEmpty() || (messageDays[0] < today && messageDays[0] < yesterday)) {
            return 0
        }

        var streak = 0
        var currentExpectedDay = if (messageDays[0] == today) today else yesterday

        for (day in messageDays) {
            if (day == currentExpectedDay) {
                streak++
                currentExpectedDay -= 24 * 60 * 60 * 1000
            } else if (day < currentExpectedDay) {
                break
            }
        }

        return streak
    }

    private fun calculateLast7DaysActivity(messages: List<MessageEntity>): List<DayActivity> {
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val days = mutableListOf<DayActivity>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dayName = dateFormat.format(cal.time)

            val startOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val count = messages.count { it.timestamp in startOfDay..endOfDay }
            days.add(DayActivity(dayName, count))
        }
        return days
    }

    private fun calculateHeatmapData(messages: List<MessageEntity>): List<HeatmapDay> {
        val endDate = LocalDate.now()
        // Last 16 weeks, starting from Sunday
        val startDate = endDate.minusWeeks(15).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

        val countsByDate = messages
            .asSequence()
            .map {
                Instant.ofEpochMilli(it.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .filter { !it.isBefore(startDate) && !it.isAfter(endDate) }
            .groupingBy { it }
            .eachCount()

        val heatmapData = mutableListOf<HeatmapDay>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            heatmapData.add(HeatmapDay(currentDate, countsByDate[currentDate] ?: 0))
            currentDate = currentDate.plusDays(1)
        }
        return heatmapData
    }

    private fun calculateTopThreads(messages: List<MessageEntity>): List<ThreadStat> {
        if (messages.isEmpty()) return emptyList()

        val sorted = messages.sortedBy { it.timestamp }
        val groups = mutableListOf<MutableList<MessageEntity>>()

        sorted.forEach { message ->
            var foundGroup = false

            if (message.repliedToId != null) {
                for (group in groups) {
                    if (group.first().id == message.repliedToId) {
                        group.add(message)
                        foundGroup = true
                        break
                    }
                }
            }

            if (!foundGroup) {
                val lastGroup = groups.lastOrNull()
                if (lastGroup != null) {
                    val lastMessage = lastGroup.last()
                    if (message.timestamp - lastMessage.timestamp < Constants.TIME_GAP_FOR_HEADER) {
                        lastGroup.add(message)
                        foundGroup = true
                    }
                }
            }

            if (!foundGroup) {
                groups.add(mutableListOf(message))
            }
        }

        val topFiveGroups = groups.sortedByDescending { it.size }.take(5)
        val maxCount = topFiveGroups.firstOrNull()?.size ?: 1

        return topFiveGroups.map { group ->
            val rootMsg = group.first()
            val title = if (rootMsg.text.isNotEmpty()) {
                rootMsg.text.take(20).plus(if (rootMsg.text.length > 20) "..." else emptyString())
            } else {
                "Media/Voice Thread"
            }

            ThreadStat(
                threadId = rootMsg.id,
                threadTitle = title,
                messageCount = group.size,
                ratio = group.size.toFloat() / maxCount
            )
        }
    }
}