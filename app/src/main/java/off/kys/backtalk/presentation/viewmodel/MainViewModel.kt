package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.use_case.CheckAppUpdate
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.state.MainUiState

/**
 * ViewModel for the main screen.
 */
class MainViewModel(
    private val application: Application,
    private val checkAppUpdate: CheckAppUpdate
) : AndroidViewModel(application) {

    /**
     * Mutable state flow for the main UI state.
     */
    private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.Idle)

    /**
     * State flow for the main UI state.
     */
    val mainUiState: StateFlow<MainUiState> = _mainUiState

    /**
     * Handles UI events for the main screen.
     *
     * @param event The UI event to handle.
     */
    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.CheckUpdate -> checkForUpdate()
            is MainUiEvent.DismissDialog -> _mainUiState.value = MainUiState.Idle
            is MainUiEvent.UpdateNow -> openUpdateUrl(event.downloadUrl)
        }
    }

    /**
     * Checks for app updates.
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

    /**
     * Opens the update URL.
     *
     * @param url The URL to open.
     */
    private fun openUpdateUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        application.startActivity(intent)
    }
}
