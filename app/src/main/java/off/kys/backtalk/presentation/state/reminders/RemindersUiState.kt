package off.kys.backtalk.presentation.state.reminders

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import off.kys.backtalk.data.local.entity.ScheduledMessageEntity

/**
 * UI state for the reminders management screen.
 */
data class RemindersUiState(
    val reminders: PersistentList<ScheduledMessageEntity> = persistentListOf(),
    val isLoading: Boolean = false
)
