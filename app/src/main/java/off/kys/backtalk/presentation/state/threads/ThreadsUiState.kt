package off.kys.backtalk.presentation.state.threads

import off.kys.backtalk.domain.model.Thread

/**
 * Data class representing the UI state of the threads screen.
 */
data class ThreadsUiState(
    val threads: List<Thread> = emptyList(),
    val isLoading: Boolean = false,
)