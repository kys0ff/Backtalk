package off.kys.backtalk.presentation.screen.messages.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import off.kys.backtalk.R
import off.kys.backtalk.presentation.components.status_scaffold.StatusAction
import off.kys.backtalk.presentation.components.status_scaffold.StatusController
import off.kys.backtalk.presentation.components.status_scaffold.StatusMessage

/**
 * Encapsulates the logic for requesting audio recording permission
 * and presenting user feedback via [StatusController].
 */
@Composable
fun rememberRecordAudioPermissionHandler(
    statusController: StatusController,
    onPermissionGranted: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val openSettingsLabel = stringResource(R.string.chat_reminder_open_settings)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.RECORD_AUDIO
            )

            if (!showRationale) {
                statusController.warning(
                    message = StatusMessage.Res(R.string.onboarding_permission_microphone_desc),
                    action = StatusAction(
                        label = openSettingsLabel,
                        onClick = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                            statusController.dismiss()
                        }
                    )
                )
            } else {
                statusController.warning(
                    message = StatusMessage.Res(R.string.onboarding_permission_microphone_desc)
                )
            }
        }
    }

    return remember(context, statusController) {
        {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGranted()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}