package off.kys.backtalk.presentation.state.threads

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import off.kys.backtalk.domain.model.Thread

/**
 * Data class representing the UI state of the threads screen.
 */
data class ThreadsUiState(
    val threads: PersistentList<Thread> = persistentListOf(),
    val isLoading: Boolean = false,
)
