package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.use_case.CheckAppUpdate
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.state.MainUiState

class MainViewModel(
    private val application: Application,
    private val checkAppUpdate: CheckAppUpdate
) : AndroidViewModel(application) {

    private val _updateState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val updateState: StateFlow<MainUiState> = _updateState

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.CheckUpdate -> checkForUpdate()
            is MainUiEvent.DismissDialog -> _updateState.value = MainUiState.Idle
            is MainUiEvent.UpdateNow -> {
                // Trigger update logic (e.g., open browser/download)
                openUpdateUrl(event.downloadUrl)
            }
        }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            _updateState.value = MainUiState.Checking
            try {
                checkAppUpdate(
                    onUpdateAvailable = { result ->
                        _updateState.value = MainUiState.UpdateAvailable(result)
                    },
                    onUpToDate = {
                        _updateState.value = MainUiState.UpToDate
                    }
                )
            } catch (e: Exception) {
                _updateState.value = MainUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun openUpdateUrl(url: String) {
       val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
         application.startActivity(intent)
    }
}
