package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import off.kys.backtalk.R

@Composable
fun SyncingProgressDialog() {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        confirmButton = { },
        title = {
            Text(
                text = stringResource(R.string.sync_status_syncing),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(R.string.common_please_wait),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
