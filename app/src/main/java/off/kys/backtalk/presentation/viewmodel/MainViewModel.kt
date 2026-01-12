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

    private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val mainUiState: StateFlow<MainUiState> = _mainUiState

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.CheckUpdate -> checkForUpdate()
            is MainUiEvent.DismissDialog -> _mainUiState.value = MainUiState.Idle
            is MainUiEvent.UpdateNow -> {
                // Trigger update logic (e.g., open browser/download)
                openUpdateUrl(event.downloadUrl)
            }
        }
    }

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

    private fun openUpdateUrl(url: String) {
       val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
         application.startActivity(intent)
    }
}
