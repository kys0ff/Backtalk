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

class MainViewModel(
    private val application: Application,
    private val checkAppUpdate: CheckAppUpdate
) : AndroidViewModel(application) {

    private val _updateState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    val updateState: StateFlow<AppUpdateState> = _updateState

    fun onEvent(event: AppUpdateEvent) {
        when (event) {
            is AppUpdateEvent.CheckUpdate -> checkForUpdate()
            is AppUpdateEvent.DismissDialog -> _updateState.value = AppUpdateState.Idle
            is AppUpdateEvent.UpdateNow -> {
                // Trigger update logic (e.g., open browser/download)
                openUpdateUrl(event.downloadUrl)
            }
        }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            _updateState.value = AppUpdateState.Checking
            try {
                checkAppUpdate(
                    onUpdateAvailable = { result ->
                        _updateState.value = AppUpdateState.UpdateAvailable(result)
                    },
                    onUpToDate = {
                        _updateState.value = AppUpdateState.UpToDate
                    }
                )
            } catch (e: Exception) {
                _updateState.value = AppUpdateState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun openUpdateUrl(url: String) {
       val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
         application.startActivity(intent)
    }
}
