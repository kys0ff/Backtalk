package off.kys.backtalk.presentation.screen.onboarding.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.state.OnboardingUiState

@Composable
fun PermissionSection(
    state: OnboardingUiState,
    onUpdatePermissions: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        onUpdatePermissions()
    }

    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onUpdatePermissions()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PermissionItem(
            title = stringResource(R.string.onboarding_permission_notifications),
            description = stringResource(R.string.onboarding_permission_notifications_desc),
            icon = R.drawable.round_update_24,
            isGranted = state.notificationPermissionGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        PermissionItem(
            title = stringResource(R.string.onboarding_permission_microphone),
            description = stringResource(R.string.onboarding_permission_microphone_desc),
            icon = R.drawable.round_keyboard_voice_24,
            isGranted = state.microphonePermissionGranted,
            onRequest = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionItem(
                title = stringResource(R.string.onboarding_permission_alarms),
                description = stringResource(R.string.onboarding_permission_alarms_desc),
                icon = R.drawable.round_access_alarm_24,
                isGranted = state.exactAlarmPermissionGranted,
                onRequest = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        }

        PermissionItem(
            title = stringResource(R.string.onboarding_permission_camera),
            description = stringResource(R.string.onboarding_permission_camera_desc),
            icon = R.drawable.round_videocam_24,
            isGranted = state.cameraPermissionGranted,
            onRequest = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )

        PermissionItem(
            title = stringResource(R.string.onboarding_permission_media),
            description = stringResource(R.string.onboarding_permission_media_desc),
            icon = R.drawable.round_image_24,
            isGranted = state.mediaPermissionGranted,
            onRequest = {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        multiplePermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                            )
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                        multiplePermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES
                            )
                        )
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        )
    }
}