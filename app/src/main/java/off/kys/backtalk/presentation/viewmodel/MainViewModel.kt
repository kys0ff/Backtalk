package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.worker.ReminderWorker
import off.kys.backtalk.domain.use_case.CheckAppUpdate
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.state.MainUiState

/**
 * ViewModel for the MainActivity.
 *
 * Handles app-wide logic such as update checks and reminder management.
 *
 * @param checkAppUpdate Use case to check for application updates.
 * @param preferences Application-wide preferences.
 * @param application The Android application context.
 */
class MainViewModel(
    private val checkAppUpdate: CheckAppUpdate,
    val preferences: BacktalkPreferences,
    private val application: Application
) : AndroidViewModel(application) {

    private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val mainUiState = _mainUiState.asStateFlow()

    init {
        // Clear unread reminder flag when the app is opened
        if (preferences.hasUnreadReminder) {
            preferences.hasUnreadReminder = false
            
            // Re-schedule smart reminder to keep it updated and avoid the "abandoned" state
            if (preferences.remindersEnabled && preferences.reminderInterval == ExportInterval.SMART) {
                ReminderWorker.scheduleSmartReminder(application)
            }
        }

        // Automatically check for updates if enabled
        if (preferences.autoUpdateEnabled) {
            onEvent(MainUiEvent.CheckUpdate)
        }
    }

    /**
     * Handles UI events for the main screen.
     */
    fun onEvent(event: MainUiEvent) {
        when (event) {
            MainUiEvent.CheckUpdate -> checkForUpdates()
            MainUiEvent.DismissDialog -> {
                _mainUiState.value = MainUiState.Idle
            }
            is MainUiEvent.UpdateNow -> {
                openUpdateUrl(event.downloadUrl)
            }
        }
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            _mainUiState.value = MainUiState.Checking
            checkAppUpdate(
                onUpdateAvailable = { result ->
                    _mainUiState.value = MainUiState.UpdateAvailable(result)
                },
                onUpToDate = {
                    _mainUiState.value = MainUiState.UpToDate
                }
            )
        }
    }

    private fun openUpdateUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            application.startActivity(intent)
        } catch (_: Exception) {
            _mainUiState.value = MainUiState.Error(messageRes = R.string.error_update_url)
        }
    }
}
