package off.kys.backtalk.presentation.state

import off.kys.github_app_updater.model.updater.UpdateResult

/**
 * Sealed interface representing the main UI state.
 */
sealed interface MainUiState {
    /**
     * Idle state.
     */
    object Idle : MainUiState

    /**
     * Checking state.
     */
    object Checking : MainUiState

    /**
     * Up to date state.
     */
    object UpToDate : MainUiState

    /**
     * Update available state.
     *
     * @param result The update result.
     */
    data class UpdateAvailable(val result: UpdateResult) : MainUiState

    /**
     * Error state.
     *
     * @param message The error message.
     */
    data class Error(val message: String) : MainUiState
}