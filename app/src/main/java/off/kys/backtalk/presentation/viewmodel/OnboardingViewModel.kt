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
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.event.OnboardingUiEvent
import off.kys.backtalk.presentation.state.onboarding.OnboardingUiState

class OnboardingViewModel(
    private val application: Application,
    private val alarmScheduler: AlarmScheduler,
    private val preferences: BacktalkPreferences
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state = _state.asStateFlow()

    init {
        updatePermissionStates()
    }

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.UpdatePermissions -> updatePermissionStates()
            OnboardingUiEvent.CompleteOnboarding -> completeOnboarding()
        }
    }

    private fun updatePermissionStates() {
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

            val cameraGranted = ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            val mediaGranted = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    ContextCompat.checkSelfPermission(application, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(application, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    ContextCompat.checkSelfPermission(application, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                }
                else -> {
                    ContextCompat.checkSelfPermission(application, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            }

            _state.update {
                it.copy(
                    notificationPermissionGranted = notificationGranted,
                    microphonePermissionGranted = microphoneGranted,
                    exactAlarmPermissionGranted = alarmGranted,
                    cameraPermissionGranted = cameraGranted,
                    mediaPermissionGranted = mediaGranted
                )
            }
        }
    }

    private fun completeOnboarding() {
        preferences.firstLaunch = false
    }
}