package off.kys.backtalk.presentation.activity.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R
import off.kys.github_app_updater.model.updater.UpdateResult

/**
 * Composable for displaying an app update dialog.
 *
 * @param updateResult The result of the update check.
 * @param onDismissRequest The callback to be invoked when the dialog is dismissed.
 * @param onUpdateClick The callback to be invoked when the update button is clicked.
 */
@Composable
fun AppUpdateDialog(
    updateResult: UpdateResult,
    onDismissRequest: () -> Unit,
    onUpdateClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(stringResource(R.string.update_available)) },
        text = {
            Text(
                stringResource(
                    R.string.a_new_version_is_available,
                    updateResult.latestVersion,
                    updateResult.changeLog
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onUpdateClick() }) {
                Text(stringResource(R.string.update_now))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.later))
            }
        }
    )
}
