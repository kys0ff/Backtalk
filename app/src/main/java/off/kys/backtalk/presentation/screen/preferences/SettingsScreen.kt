package off.kys.backtalk.presentation.screen.preferences

import android.provider.DocumentsContract
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
        val mainActivity = LocalActivity.current as? MainActivity
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val viewModel = koinViewModel<SettingsViewModel>()
        val state by viewModel.state.collectAsState(SettingsUiState())

        val showExportDialog = remember { mutableStateOf(false) }
        val showImportStrategyDialog = remember { mutableStateOf(false) }
        val showPasswordDialog = remember { mutableStateOf(false) }
        val showIntervalDialog = remember { mutableStateOf(false) }

        var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
        var isImporting by remember { mutableStateOf(false) }

        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let {
                selectedUri = it
                showExportDialog.value = true
            }
        }

        val folderLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            uri?.let {
                viewModel.onEvent(SettingsUiEvent.OnAutoExportFolderChange(it))
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
            if (state.isBackupEncrypted != null) {
                showImportStrategyDialog.value = true
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
                Column {
                    SettingsTopAppBar(
                        onNavigateBack = { navigator.pop() },
                        scrollBehavior = scrollBehavior
                    )
                    if (state.backupLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                PreferenceCategory(stringResource(R.string.auto_export))

                ToggleSetting(
                    label = stringResource(R.string.auto_export),
                    supportingText = stringResource(R.string.auto_export_desc),
                    icon = painterResource(R.drawable.round_update_24),
                    checked = state.autoExportEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && state.autoExportUri == null) {
                            folderLauncher.launch(null)
                        } else {
                            viewModel.onEvent(SettingsUiEvent.OnAutoExportToggle(enabled))
                        }
                    }
                )

                AnimatedVisibility(visible = state.autoExportEnabled) {
                    Column {
                        val noFolderSelected = stringResource(R.string.no_folder_selected)
                        val folderName = remember(state.autoExportUri, noFolderSelected) {
                            state.autoExportUri?.let { uriString ->
                                val uri = uriString.toUri()
                                androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)?.name
                            } ?: noFolderSelected
                        }

                        InfoRow(
                            label = stringResource(R.string.backup_folder),
                            value = folderName,
                            icon = painterResource(R.drawable.round_description_24),
                            onClick = { folderLauncher.launch(null) }
                        )

                        InfoRow(
                            label = stringResource(R.string.export_interval),
                            value = stringResource(state.autoExportInterval.titleResId),
                            icon = painterResource(R.drawable.round_refresh_24),
                            onClick = { showIntervalDialog.value = true }
                        )

                        ToggleSetting(
                            label = stringResource(R.string.encrypt_auto_export),
                            icon = painterResource(if (state.autoExportEncrypted) R.drawable.round_lock_24 else R.drawable.round_lock_open_24),
                            checked = state.autoExportEncrypted,
                            onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnAutoExportEncryptionToggle(it)) }
                        )

                        AnimatedVisibility(visible = state.autoExportEncrypted) {
                            var passwordVisible by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = state.autoExportPassword ?: "",
                                onValueChange = { viewModel.onEvent(SettingsUiEvent.OnAutoExportPasswordChange(it)) },
                                label = { Text(stringResource(R.string.auto_export_password)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            painter = painterResource(if (passwordVisible) R.drawable.round_visibility_24 else R.drawable.round_visibility_off_24),
                                            contentDescription = null
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                        }
                    }
                }

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
                            mainActivity?.checkForUpdates()
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
        if (showExportDialog.value) {
            ExportDialog(
                onDismiss = {
                    showExportDialog.value = false
                    selectedUri?.let { uri ->
                        runCatching {
                            DocumentsContract.deleteDocument(context.contentResolver, uri)
                        }
                    }
                    selectedUri = null
                },
                onConfirm = { exportPassword ->
                    showExportDialog.value = false
                    selectedUri?.let { uri ->
                        viewModel.onEvent(
                            SettingsUiEvent.ExportBackup(uri, exportPassword)
                        )
                    }
                    selectedUri = null
                }
            )
        }

        if (showImportStrategyDialog.value) {
            ImportStrategyDialog(
                onDismiss = {
                    showImportStrategyDialog.value = false
                    viewModel.onEvent(SettingsUiEvent.ResetBackupState)
                },
                onConfirm = { clearAndImport ->
                    showImportStrategyDialog.value = false
                    isImporting = clearAndImport
                    if (state.isBackupEncrypted == true) {
                        showPasswordDialog.value = true
                    } else {
                        state.selectedBackupUri?.let { uri ->
                            viewModel.onEvent(
                                SettingsUiEvent.ImportBackup(uri, null, clearAndImport)
                            )
                        }
                    }
                }
            )
        }

        if (showPasswordDialog.value || state.wrongPasswordError) {
            PasswordDialog(
                wrongPasswordError = state.wrongPasswordError,
                onDismiss = {
                    showPasswordDialog.value = false
                    viewModel.onEvent(SettingsUiEvent.ResetBackupState)
                },
                onConfirm = { enteredPassword ->
                    showPasswordDialog.value = false
                    state.selectedBackupUri?.let { uri ->
                        viewModel.onEvent(
                            SettingsUiEvent.ImportBackup(
                                uri,
                                enteredPassword.ifBlank { null },
                                isImporting
                            )
                        )
                    }
                }
            )
        }

        if (showIntervalDialog.value) {
            IntervalSelectionDialog(
                selected = state.autoExportInterval,
                onDismiss = { showIntervalDialog.value = false },
                onSelected = {
                    viewModel.onEvent(SettingsUiEvent.OnAutoExportIntervalChange(it))
                    showIntervalDialog.value = false
                }
            )
        }
    }

    @Composable
    private fun IntervalSelectionDialog(
        selected: off.kys.backtalk.common.ExportInterval,
        onDismiss: () -> Unit,
        onSelected: (off.kys.backtalk.common.ExportInterval) -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.export_interval)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    off.kys.backtalk.common.ExportInterval.entries.forEach { interval ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (interval == selected),
                                    onClick = { onSelected(interval) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (interval == selected), onClick = null)
                            Text(
                                text = stringResource(interval.titleResId),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    @Composable
    private fun ExportDialog(
        onDismiss: () -> Unit,
        onConfirm: (String?) -> Unit
    ) {
        var useEncryption by remember { mutableStateOf(false) }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.round_file_upload_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = stringResource(R.string.export_backup))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.backup_password_prompt),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Encryption Toggle Row
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        onClick = { useEncryption = !useEncryption }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(if (useEncryption) R.drawable.round_lock_24 else R.drawable.round_lock_open_24),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (useEncryption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.security),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = useEncryption,
                                onCheckedChange = { useEncryption = it },
                                thumbContent = if (useEncryption) {
                                    {
                                        Icon(
                                            painter = painterResource(R.drawable.round_check_24),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    // Animated visibility for the password field
                    AnimatedVisibility(visible = useEncryption) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.enter_password)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(if (passwordVisible) R.drawable.round_visibility_24 else R.drawable.round_visibility_off_24),
                                        contentDescription = null
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }
                }
            },
            confirmButton = {
                val isReady = !useEncryption || password.isNotBlank()
                Button(
                    onClick = { onConfirm(if (useEncryption) password else null) },
                    enabled = isReady
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    @Composable
    fun ImportStrategyDialog(
        onDismiss: () -> Unit,
        onConfirm: (Boolean) -> Unit
    ) {
        // Default to 'false' (Merge) to be safe
        var clearData by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painterResource(R.drawable.round_file_download_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = stringResource(R.string.import_strategy))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.import_strategy_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Option 1: Merge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = !clearData,
                                onClick = { clearData = false },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = !clearData, onClick = null)
                        Text(
                            text = stringResource(R.string.merge_data),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    // Option 2: Clear and Import (The "Danger" option)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = clearData,
                                onClick = { clearData = true },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = clearData,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.error
                            )
                        )
                        Text(
                            text = stringResource(R.string.clear_and_import),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (clearData) MaterialTheme.colorScheme.error else Color.Unspecified,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(clearData) },
                    colors = if (clearData) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    @Composable
    private fun PasswordDialog(
        wrongPasswordError: Boolean,
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        var password by remember { mutableStateOf(emptyString()) }
        var passwordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painterResource(R.drawable.round_lock_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            title = {
                Text(text = stringResource(R.string.enter_password))
            },
            text = {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.enter_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = wrongPasswordError,
                    supportingText = {
                        if (wrongPasswordError) {
                            Text(text = stringResource(R.string.incorrect_password_please_try_again))
                        }
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image =
                            painterResource(if (passwordVisible) R.drawable.round_visibility_24 else R.drawable.round_visibility_off_24)
                        val description =
                            stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password)

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painter = image, contentDescription = description)
                        }
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(password) },
                    enabled = password.isNotBlank()
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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