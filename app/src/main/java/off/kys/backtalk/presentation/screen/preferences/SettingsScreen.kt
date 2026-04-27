package off.kys.backtalk.presentation.screen.preferences

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.presentation.activity.MainActivity
import off.kys.backtalk.presentation.event.SettingsUiEvent
import off.kys.backtalk.presentation.state.SettingsUiState
import off.kys.backtalk.presentation.viewmodel.SettingsViewModel
import off.kys.backtalk.util.emptyString
import off.kys.backtalk.util.isSecurityEnabled
import off.kys.backtalk.util.openUrl
import off.kys.backtalk.util.toast
import org.koin.compose.viewmodel.koinViewModel

/**
 * The settings screen of the application.
 */
class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val mainActivity = LocalActivity.current as MainActivity
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val viewModel = koinViewModel<SettingsViewModel>()
        val state by viewModel.state.collectAsState(SettingsUiState())

        var showExportDialog by remember { mutableStateOf(false) }
        var showImportStrategyDialog by remember { mutableStateOf(false) }
        var showPasswordDialog by remember { mutableStateOf(false) }

        var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
        var password by remember { mutableStateOf("") }
        var isImporting by remember { mutableStateOf(false) }

        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let {
                selectedUri = it
                showExportDialog = true
            }
        }

        val importLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                viewModel.onEvent(SettingsUiEvent.CheckBackupEncryption(it))
            }
        }

        LaunchedEffect(state.isBackupEncrypted) {
            if (state.isBackupEncrypted == false) {
                showImportStrategyDialog = true
            } else if (state.isBackupEncrypted == true) {
                showImportStrategyDialog = true
            }
        }

        LaunchedEffect(state.error) {
            state.error?.let {
                context.toast(it)
                viewModel.onEvent(SettingsUiEvent.ClearError)
            }
        }

        LaunchedEffect(state.successMessage) {
            state.successMessage?.let {
                context.toast(it)
                viewModel.onEvent(SettingsUiEvent.ClearSuccess)
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SettingsTopAppBar(
                    onNavigateBack = { navigator.pop() },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (state.backupLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                PreferenceCategory(stringResource(R.string.appearance))

                ThemeSelector(
                    selected = state.themeMode,
                    onSelected = { viewModel.onEvent(SettingsUiEvent.OnThemeModeChange(it)) }
                )

                ToggleSetting(
                    label = stringResource(R.string.material_you_dynamic_color),
                    supportingText = stringResource(R.string.apply_system_colors_to_the_app_interface),
                    icon = painterResource(R.drawable.round_palette_24),
                    checked = state.dynamicColorEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnDynamicColorToggle(it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                PreferenceCategory(stringResource(R.string.privacy_security))

                if (context.isSecurityEnabled()) {
                    ToggleSetting(
                        label = stringResource(R.string.enable_app_lock),
                        icon = painterResource(R.drawable.round_lock_24),
                        checked = state.lockEnabled,
                        requireRestart = true,
                        onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnLockToggle(it)) }
                    )
                }

                ToggleSetting(
                    label = stringResource(R.string.secure_screen_block_screenshots),
                    icon = painterResource(R.drawable.round_screen_lock_portrait_24),
                    checked = state.secureScreenEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnSecureScreenToggle(it)) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                PreferenceCategory(stringResource(R.string.backup_restore))

                InfoRow(
                    label = stringResource(R.string.export_backup),
                    value = stringResource(R.string.export_backup_desc),
                    icon = painterResource(R.drawable.round_send_24),
                    onClick = { exportLauncher.launch("backtalk_backup_${System.currentTimeMillis()}.json") }
                )

                InfoRow(
                    label = stringResource(R.string.import_backup),
                    value = stringResource(R.string.import_backup_desc),
                    icon = painterResource(R.drawable.round_reply_24),
                    onClick = {
                        importLauncher.launch(
                            arrayOf(
                                "application/json",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    }
                )

                if (!BuildConfig.IS_FDROID) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 8.dp,
                            horizontal = 16.dp
                        )
                    )
                    PreferenceCategory(stringResource(R.string.updates))

                    ToggleSetting(
                        label = stringResource(R.string.auto_check_updates),
                        supportingText = stringResource(R.string.auto_check_updates_desc),
                        icon = painterResource(R.drawable.round_update_24),
                        checked = state.autoUpdateEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnAutoUpdateToggle(it)) }
                    )

                    InfoRow(
                        label = stringResource(R.string.check_for_updates_now),
                        value = stringResource(R.string.tap_to_check_latest_version),
                        icon = painterResource(R.drawable.round_refresh_24),
                        onClick = {
                            context.toast(R.string.checking_for_updates)
                            mainActivity.checkForUpdates()
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                PreferenceCategory(stringResource(R.string.about))

                InfoRow(
                    label = stringResource(R.string.version),
                    value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    icon = painterResource(R.drawable.round_info_24)
                )

                InfoRow(
                    label = stringResource(R.string.developer),
                    value = stringResource(R.string.dev_name),
                    icon = painterResource(R.drawable.round_person_24),
                    onClick = { context.toast(R.string.dev_click) }
                )

                InfoRow(
                    label = stringResource(R.string.license),
                    value = stringResource(R.string.mit),
                    icon = painterResource(R.drawable.round_description_24),
                    onClick = { context.openUrl(Constants.BACKTALK_MIT_LICENSE_RAW_URL) }
                )
            }
        }

        // Dialogs
        if (showExportDialog) {
            var useEncryption by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text(stringResource(R.string.export_backup)) },
                text = {
                    Column {
                        Text(stringResource(R.string.backup_password_prompt))
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.security))
                            Spacer(Modifier.weight(1f))
                            Switch(
                                checked = useEncryption,
                                onCheckedChange = { useEncryption = it })
                        }
                        if (useEncryption) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(stringResource(R.string.enter_password)) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showExportDialog = false
                        selectedUri?.let { uri ->
                            viewModel.onEvent(
                                SettingsUiEvent.ExportBackup(
                                    uri,
                                    if (useEncryption) password else null
                                )
                            )
                        }
                        password = ""
                    }) {
                        Text(stringResource(R.string.send))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (showImportStrategyDialog) {
            AlertDialog(
                onDismissRequest = { showImportStrategyDialog = false },
                title = { Text(stringResource(R.string.import_strategy)) },
                text = { Text(stringResource(R.string.import_strategy_desc)) },
                confirmButton = {
                    Row {
                        TextButton(
                            onClick = {
                                showImportStrategyDialog = false
                                isImporting = false // clearExisting is false for merge
                                if (state.isBackupEncrypted == true) {
                                    showPasswordDialog = true
                                } else {
                                    state.selectedBackupUri?.let { uri ->
                                        viewModel.onEvent(SettingsUiEvent.ImportBackup(uri, null, false))
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.merge_data))
                        }
                        TextButton(
                            onClick = {
                                showImportStrategyDialog = false
                                isImporting = true // clearExisting is true for clear_and_import
                                if (state.isBackupEncrypted == true) {
                                    showPasswordDialog = true
                                } else {
                                    state.selectedBackupUri?.let { uri ->
                                        viewModel.onEvent(SettingsUiEvent.ImportBackup(uri, null, true))
                                    }
                                }
                            }
                        ) {
                            Text(
                                stringResource(R.string.clear_and_import),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportStrategyDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (showPasswordDialog || state.wrongPasswordError) {
            AlertDialog(
                onDismissRequest = { 
                    showPasswordDialog = false
                    viewModel.onEvent(SettingsUiEvent.ResetBackupState)
                },
                title = { Text(stringResource(R.string.enter_password)) },
                text = {
                    Column {
                        if (state.wrongPasswordError) {
                            Text(
                                text = stringResource(R.string.incorrect_password_please_try_again),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.enter_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.wrongPasswordError
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showPasswordDialog = false
                        state.selectedBackupUri?.let { uri ->
                            viewModel.onEvent(
                                SettingsUiEvent.ImportBackup(
                                    uri,
                                    password.ifBlank { null },
                                    isImporting
                                )
                            )
                        }
                        password = emptyString()
                    }) {
                        Text(stringResource(R.string.submit))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showPasswordDialog = false
                        viewModel.onEvent(SettingsUiEvent.ResetBackupState)
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SettingsTopAppBar(
        onNavigateBack: () -> Unit,
        scrollBehavior: TopAppBarScrollBehavior
    ) {
        LargeTopAppBar(
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.round_arrow_back_24),
                        contentDescription = stringResource(R.string.navigate_up)
                    )
                }
            },
            title = { Text(text = stringResource(R.string.settings)) },
            scrollBehavior = scrollBehavior
        )
    }

    @Composable
    private fun PreferenceCategory(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )
    }

    @Composable
    private fun ToggleSetting(
        label: String,
        icon: Painter? = null,
        supportingText: String? = null,
        checked: Boolean,
        requireRestart: Boolean = false,
        onCheckedChange: (Boolean) -> Unit
    ) {
        val context = LocalContext.current
        ListItem(
            headlineContent = { Text(label) },
            supportingContent = supportingText?.let { { Text(it) } },
            leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        onCheckedChange(it)
                        if (requireRestart) context.toast(R.string.restart_app_to_apply_changes)
                    }
                )
            },
            modifier = Modifier.clickable { onCheckedChange(!checked) }
        )
    }

    @Composable
    private fun InfoRow(
        label: String,
        value: String,
        icon: Painter? = null,
        onClick: (() -> Unit)? = null
    ) {
        ListItem(
            headlineContent = { Text(label) },
            supportingContent = { Text(value) },
            leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
            modifier = Modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        )
    }

    @Composable
    private fun ThemeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
        Column(Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                ListItem(
                    headlineContent = {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    leadingContent = {
                        RadioButton(
                            selected = (mode == selected),
                            onClick = null
                        )
                    },
                    modifier = Modifier.clickable { onSelected(mode) }
                )
            }
        }
    }
}