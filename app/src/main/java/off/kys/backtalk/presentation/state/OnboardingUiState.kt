package off.kys.backtalk.presentation.state

data class OnboardingUiState(
    val notificationPermissionGranted: Boolean = false,
    val microphonePermissionGranted: Boolean = false,
    val exactAlarmPermissionGranted: Boolean = false
)