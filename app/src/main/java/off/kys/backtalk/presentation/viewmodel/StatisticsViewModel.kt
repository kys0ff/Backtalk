package off.kys.backtalk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.presentation.state.DayActivity
import off.kys.backtalk.presentation.state.StatisticsUiState
import off.kys.backtalk.presentation.state.ThreadStat
import java.text.SimpleDateFormat
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
            
            val voiceMessages = allMessages.filter { it.voicePath != null }
            val textMessages = allMessages.filter { it.voicePath == null }
            
            val totalVoiceDuration = voiceMessages.sumOf { it.voiceDuration ?: 0L }
            val avgLen = if (textMessages.isNotEmpty()) {
                textMessages.sumOf { it.text.length } / textMessages.size
            } else 0

            val activity = calculateLast7DaysActivity(allMessages)
            val topThreads = calculateTopThreads(allMessages)

            _state.update {
                it.copy(
                    totalMessages = allMessages.size,
                    voiceMessagesCount = voiceMessages.size,
                    textMessagesCount = textMessages.size,
                    totalVoiceDurationMs = totalVoiceDuration,
                    scheduledMessagesCount = scheduledMessages.size,
                    activityLast7Days = activity,
                    topThreads = topThreads,
                    avgMessageLength = avgLen,
                    isLoading = false
                )
            }
        }
    }

    private fun calculateLast7DaysActivity(messages: List<MessageEntity>): List<DayActivity> {
        val calendar = Calendar.getInstance()
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

    private fun calculateTopThreads(messages: List<MessageEntity>): List<ThreadStat> {
        // A thread starts with a message that has repliedToId == null
        // Replies have repliedToId pointing to the root message (or another message in the chain)
        // For simplicity, let's group by rootId. If repliedToId is null, it's a root.
        // If not null, we'd need to trace back, but let's assume repliedToId is the rootId for now
        // based on how the app seems to handle threads (one level deep or direct replies).
        
        val threadGroups = messages.groupBy { it.repliedToId?.value ?: it.id.value }
        val sortedThreads = threadGroups.map { (rootId, msgs) ->
            val rootMsg = messages.find { it.id.value == rootId }
            val title = rootMsg?.text?.take(20)?.plus(if (rootMsg.text.length > 20) "..." else "") 
                ?: "Unknown Thread"
            
            ThreadStat(
                threadId = rootMsg?.id ?: msgs.first().id,
                threadTitle = title,
                messageCount = msgs.size,
                ratio = 0f // will calculate after
            )
        }.sortedByDescending { it.messageCount }.take(5)

        val maxCount = sortedThreads.firstOrNull()?.messageCount ?: 1
        return sortedThreads.map { it.copy(ratio = it.messageCount.toFloat() / maxCount) }
    }
}
