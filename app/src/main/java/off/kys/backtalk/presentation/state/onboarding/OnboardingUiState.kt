package off.kys.backtalk.presentation.state.onboarding

data class OnboardingUiState(
    val notificationPermissionGranted: Boolean = false,
    val microphonePermissionGranted: Boolean = false,
    val exactAlarmPermissionGranted: Boolean = false,
    val cameraPermissionGranted: Boolean = false,
    val mediaPermissionGranted: Boolean = false
)