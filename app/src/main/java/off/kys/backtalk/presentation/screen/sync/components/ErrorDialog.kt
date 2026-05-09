package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import off.kys.backtalk.R
import off.kys.backtalk.presentation.viewmodel.SyncViewModel

@Composable
fun ErrorDialog(error: String, viewModel: SyncViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.clearError() },
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_warning_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.common_error),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { viewModel.clearError() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_ok))
            }
        }
    )
}