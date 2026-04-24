package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.use_case.CheckAppUpdate
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.util.openUrl

/**
 * ViewModel for the main screen.
 *
 * This ViewModel manages the UI state for the main activity and coordinates
 * app update checks using the [CheckAppUpdate] use case.
 *
 * @param application The [Application] context.
 * @param checkAppUpdate The use case to check for application updates.
 */
class MainViewModel(
    private val application: Application,
    private val checkAppUpdate: CheckAppUpdate
) : AndroidViewModel(application) {

    /**
     * Internal mutable state flow for the main UI.
     */
    private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.Idle)

    /**
     * Publicly exposed state flow for the main UI.
     * Observers can use this to react to changes in the UI state.
     */
    val mainUiState: StateFlow<MainUiState> = _mainUiState

    /**
     * Processes UI events originating from the main screen.
     *
     * @param event The [MainUiEvent] to handle, such as checking for updates or dismissing dialogs.
     */
    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.CheckUpdate -> checkForUpdate()
            is MainUiEvent.DismissDialog -> _mainUiState.value = MainUiState.Idle
            is MainUiEvent.UpdateNow -> application.openUrl(event.downloadUrl)
        }
    }

    /**
     * Initiates a check for app updates.
     *
     * This method launches a coroutine to perform the update check. It updates
     * the [_mainUiState] to reflect the current progress and results:
     * - [MainUiState.Checking]: While the update check is in progress.
     * - [MainUiState.UpdateAvailable]: If a new version is found.
     * - [MainUiState.UpToDate]: If the app is already at the latest version.
     * - [MainUiState.Error]: If an exception occurs during the check.
     */
    private fun checkForUpdate() {
        viewModelScope.launch {
            _mainUiState.value = MainUiState.Checking
            try {
                checkAppUpdate(
                    onUpdateAvailable = { result ->
                        _mainUiState.value = MainUiState.UpdateAvailable(result)
                    },
                    onUpToDate = {
                        _mainUiState.value = MainUiState.UpToDate
                    }
                )
            } catch (e: Exception) {
                _mainUiState.value = MainUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
