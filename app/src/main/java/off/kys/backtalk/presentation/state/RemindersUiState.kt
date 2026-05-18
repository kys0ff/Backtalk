package off.kys.backtalk.presentation.state

import off.kys.backtalk.data.local.entity.ScheduledMessageEntity

/**
 * UI state for the reminders management screen.
 */
data class RemindersUiState(
    val reminders: List<ScheduledMessageEntity> = emptyList(),
    val isLoading: Boolean = false
)