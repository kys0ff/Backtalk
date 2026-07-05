package off.kys.backtalk.presentation.state.main

import off.kys.github_app_updater_lib.model.updater.UpdateResult

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
     * Up-to-date state.
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
     * @param messageRes The error message resource ID.
     */
    data class Error(
        val message: String? = null,
        val messageRes: Int? = null
    ) : MainUiState
}