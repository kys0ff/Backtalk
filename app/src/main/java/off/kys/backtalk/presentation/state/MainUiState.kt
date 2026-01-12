package off.kys.backtalk.presentation.state

import off.kys.github_app_updater.model.updater.UpdateResult

sealed interface MainUiState {
    object Idle : MainUiState
    object Checking : MainUiState
    object UpToDate : MainUiState
    data class UpdateAvailable(val result: UpdateResult) : MainUiState
    data class Error(val message: String) : MainUiState
}