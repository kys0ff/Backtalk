package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.viewmodel.SyncViewModel

@Composable
fun PinDisplayDialog(pin: String, viewModel: SyncViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissPinDialog() },
        title = {
            Text(
                text = stringResource(R.string.sync_pairing_pin_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.sync_pairing_pin_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.medium
                        ),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = pin,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(
                            horizontal = 32.dp,
                            vertical = 12.dp
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.dismissPinDialog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_ok))
            }
        }
    )
}
