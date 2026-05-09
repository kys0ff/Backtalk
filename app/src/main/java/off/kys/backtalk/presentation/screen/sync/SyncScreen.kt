package off.kys.backtalk.presentation.screen.sync

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.R
import off.kys.backtalk.presentation.status.SyncStatus
import off.kys.backtalk.presentation.viewmodel.SyncViewModel
import off.kys.backtalk.sync.DeviceInfo
import off.kys.backtalk.util.emptyString
import org.koin.compose.viewmodel.koinViewModel
import java.text.DateFormat

class SyncScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SyncViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        var pinInput by remember { mutableStateOf(emptyString()) }

        LaunchedEffect(Unit) {
            viewModel.cleanupInvalidDevices()
            viewModel.startDiscovery()
        }

        DisposableEffect(Unit) {
            onDispose { viewModel.stopDiscovery() }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.sync_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                painterResource(R.drawable.round_arrow_back_24),
                                stringResource(R.string.common_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (state.isDiscovering) {
                                    viewModel.stopDiscovery()
                                } else {
                                    viewModel.startDiscovery()
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(if (state.isDiscovering) R.drawable.round_close_24 else R.drawable.round_refresh_24),
                                contentDescription = stringResource(if (state.isDiscovering) R.string.sync_stop_discovery else R.string.common_search)
                            )
                        }
                    }
                )
            }
        ) { padding ->
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
                        onPairClick = { viewModel.requestPairing(device) }
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
                        onPushClick = { viewModel.syncNow(device) },
                        onPullClick = { viewModel.pullSync(device) },
                        onDisconnectClick = { viewModel.confirmUnpair(device) }
                    )
                }
            }

            // Dialogs
            state.deviceToUnpair?.let { device ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissUnpairDialog() },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.round_link_off_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.sync_disconnect),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.sync_disconnect_confirm, device.name),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.disconnect(device) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissUnpairDialog() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            state.deviceToRePair?.let { device ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissRePairDialog() },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.round_sync_problem_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.sync_already_paired_title),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.sync_already_paired_msg, device.name),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.confirmRePair(device) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.sync_unpair_and_restart))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissRePairDialog() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            state.incomingRequest?.let { device ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissIncomingRequest() },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.round_sync_problem_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.sync_pairing_request_title),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.sync_pairing_request_msg),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.acceptRequest(device) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.sync_show_pin))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.refuseRequest(device) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.common_cancel),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }

            state.pinToShow?.let { pin ->
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

            if (state.showPinDialog) {
                val focusRequester = remember { FocusRequester() }

                // Request focus automatically when dialog appears
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    pinInput = emptyString()
                }

                AlertDialog(
                    onDismissRequest = { viewModel.dismissPinDialog() },
                    title = {
                        Text(
                            text = stringResource(R.string.sync_enter_pin),
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
                                text = stringResource(R.string.sync_pin_label),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Wrap both in a Box so the TextField can overlay the visual boxes
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    repeat(6) { index ->
                                        val char = pinInput.getOrNull(index)?.toString().orEmpty()
                                        val isFocused = pinInput.length == index

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .border(
                                                    width = if (isFocused) 2.dp else 1.dp,
                                                    color = if (isFocused) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outline,
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char,
                                                style = MaterialTheme.typography.headlineMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                BasicTextField(
                                    value = pinInput,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                            pinInput = it
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Done
                                    ),
                                    modifier = Modifier
                                        .matchParentSize()
                                        .focusRequester(focusRequester)
                                        .alpha(0f)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                state.deviceBeingPaired?.let {
                                    viewModel.verifyPin(
                                        it,
                                        pinInput
                                    )
                                }
                            },
                            enabled = pinInput.length == 6,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissPinDialog() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            if (state.syncStatus == SyncStatus.SYNCING) {
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

            state.error?.let { error ->
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
        }
    }
}

@Composable
fun SectionHeader(title: String, isSearching: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        if (isSearching) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
fun DeviceItem(device: DeviceInfo, onPairClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        ),
        onClick = onPairClick
    ) {
        ListItem(
            headlineContent = { Text(device.name) },
            supportingContent = { Text(device.address.orEmpty()) },
            leadingContent = { Icon(painterResource(R.drawable.round_phone_android_24), null) },
            trailingContent = {
                IconButton(onClick = onPairClick) {
                    Icon(painterResource(R.drawable.round_add_link_24), null)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

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