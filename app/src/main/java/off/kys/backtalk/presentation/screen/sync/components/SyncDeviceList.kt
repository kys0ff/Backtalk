package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.state.preferences.SyncUiState
import off.kys.backtalk.sync.DeviceInfo

@Composable
fun SyncDeviceList(
    padding: PaddingValues,
    state: SyncUiState,
    onPairClick: (DeviceInfo) -> Unit,
    onPushClick: (DeviceInfo) -> Unit,
    onPullClick: (DeviceInfo) -> Unit,
    onDisconnectClick: (DeviceInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Available Devices Section
        item {
            SectionHeader(
                title = stringResource(R.string.sync_available_devices),
                isSearching = state.isDiscovering
            )
        }

        if (state.discoveredDevices.isEmpty() && !state.isDiscovering) {
            item {
                Text(
                    text = stringResource(R.string.sync_no_devices_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        items(state.discoveredDevices) { device ->
            DeviceItem(
                device = device,
                onPairClick = { onPairClick(device) }
            )
        }

        // Paired Devices Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.sync_paired_devices),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(state.pairedDevices) { device ->
            PairedDeviceItem(
                device = device,
                onPushClick = { onPushClick(device) },
                onPullClick = { onPullClick(device) },
                onDisconnectClick = { onDisconnectClick(device) }
            )
        }
    }
}