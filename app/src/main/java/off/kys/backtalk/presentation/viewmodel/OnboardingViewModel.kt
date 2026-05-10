package off.kys.backtalk.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.presentation.state.OnboardingUiState

class OnboardingViewModel(
    private val application: Application,
    private val alarmScheduler: AlarmScheduler
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state = _state.asStateFlow()

    init {
        updatePermissionStates()
    }

    fun updatePermissionStates() {
        viewModelScope.launch {
            val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            val microphoneGranted = ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            val alarmGranted = alarmScheduler.canScheduleExactAlarms()

            _state.update {
                it.copy(
                    notificationPermissionGranted = notificationGranted,
                    microphonePermissionGranted = microphoneGranted,
                    exactAlarmPermissionGranted = alarmGranted
                )
            }
        }
    }
}