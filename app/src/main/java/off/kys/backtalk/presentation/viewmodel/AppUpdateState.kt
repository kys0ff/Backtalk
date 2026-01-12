package off.kys.backtalk.presentation.viewmodel

import off.kys.github_app_updater.model.updater.UpdateResult

sealed interface AppUpdateState {
    object Idle : AppUpdateState
    object Checking : AppUpdateState
    object UpToDate : AppUpdateState
    data class UpdateAvailable(val result: UpdateResult) : AppUpdateState
    data class Error(val message: String) : AppUpdateState
}