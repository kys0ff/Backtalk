package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R

@Composable
fun OldBackupWarningDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_warning_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(stringResource(R.string.backup_old_format_warning_title)) },
        text = { Text(stringResource(R.string.backup_old_format_warning_message)) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.common_ok))
            }
        }
    )
}