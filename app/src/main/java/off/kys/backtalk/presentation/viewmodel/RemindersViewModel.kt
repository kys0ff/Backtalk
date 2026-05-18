package off.kys.backtalk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.state.RemindersUiState

/**
 * ViewModel for the reminders management screen.
 */
class RemindersViewModel(
    private val useCases: MessagesUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(RemindersUiState())
    val state = _state.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        _state.update { it.copy(isLoading = true) }
        useCases.getAllScheduledMessages()
            .onEach { reminders ->
                _state.update { 
                    it.copy(
                        reminders = reminders.sortedBy { r -> r.scheduledTimestamp },
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Cancels a scheduled reminder.
     */
    fun cancelReminder(id: MessageId) {
        viewModelScope.launch {
            useCases.cancelScheduledMessage(id)
        }
    }
}
