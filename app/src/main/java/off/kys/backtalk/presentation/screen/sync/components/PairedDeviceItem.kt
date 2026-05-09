package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.sync.DeviceInfo
import java.text.DateFormat

@Composable
fun PairedDeviceItem(
    device: DeviceInfo,
    onPushClick: () -> Unit,
    onPullClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder(enabled = device.isOnline)
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            ListItem(
                headlineContent = { Text(device.name, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    if (device.lastSyncTimestamp > 0) {
                        Text(
                            stringResource(
                                R.string.sync_status_last_synced,
                                DateFormat.getDateTimeInstance().format(device.lastSyncTimestamp)
                            )
                        )
                    }
                },
                trailingContent = {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = if (device.isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (device.isOnline) stringResource(R.string.sync_status_online) else stringResource(
                                R.string.sync_status_offline
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (device.isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDisconnectClick) {
                    Icon(
                        painterResource(R.drawable.round_link_off_24),
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = onPullClick,
                    enabled = device.isOnline,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.round_file_download_24),
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.sync_pull),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onPushClick,
                    enabled = device.isOnline,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.round_sync_24),
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.sync_now),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}