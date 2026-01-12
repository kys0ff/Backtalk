package off.kys.backtalk.presentation.activity.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R
import off.kys.github_app_updater.model.updater.UpdateResult

@Composable
fun AppUpdateDialog(
    updateResult: UpdateResult,
    onDismissRequest: () -> Unit,
    onUpdateClick:()->Unit
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
            Button(onClick = { onUpdateClick() }
            ) {
                Text(stringResource(R.string.update_now))
            }
        },
        dismissButton = {
            Button(onClick = { onDismissRequest() }
            ) {
                Text(stringResource(R.string.later))
            }
        }
    )
}
