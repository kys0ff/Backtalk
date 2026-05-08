package off.kys.backtalk.presentation.screen.preferences

import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.presentation.activity.MainActivity
import off.kys.backtalk.presentation.event.SettingsUiEvent
import off.kys.backtalk.presentation.state.SettingsUiState
import off.kys.backtalk.presentation.viewmodel.SettingsViewModel
import off.kys.backtalk.util.emptyString
import off.kys.backtalk.util.isSecurityEnabled
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
        val showAutoExportPasswordDialog = remember { mutableStateOf(false) }
        val showThemeDialog = remember { mutableStateOf(false) }
        val showOldBackupWarning = remember { mutableStateOf(false) }
        val showWipeDataDialog = remember { mutableStateOf(false) }

        var selectedUri by remember { mutableStateOf<Uri?>(null) }
        var isImporting by remember { mutableStateOf(false) }
        var devClickCount by remember { mutableIntStateOf(0) }

        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/octet-stream")
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

        LaunchedEffect(state.showOldBackupWarning) {
            if (state.showOldBackupWarning) {
                showOldBackupWarning.value = true
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Appearance Section
                SettingsSection(title = stringResource(R.string.settings_appearance)) {
                    SettingsItem(
                        label = stringResource(R.string.settings_theme),
                        value = state.themeMode.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        icon = painterResource(R.drawable.round_brightness_6_24),
                        onClick = { showThemeDialog.value = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsToggle(
                        label = stringResource(R.string.settings_dynamic_color),
                        supportingText = stringResource(R.string.settings_dynamic_color_desc),
                        icon = painterResource(R.drawable.round_palette_24),
                        checked = state.dynamicColorEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(
                                SettingsUiEvent.OnDynamicColorToggle(
                                    it
                                )
                            )
                        }
                    )
                }

                // Security Section
                SettingsSection(title = stringResource(R.string.settings_privacy_security)) {
                    if (context.isSecurityEnabled()) {
                        SettingsToggle(
                            label = stringResource(R.string.settings_enable_app_lock),
                            supportingText = stringResource(R.string.settings_app_lock_desc),
                            icon = painterResource(R.drawable.round_lock_24),
                            checked = state.lockEnabled,
                            onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnLockToggle(it)) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    SettingsToggle(
                        label = stringResource(R.string.settings_secure_screen),
                        supportingText = stringResource(R.string.settings_secure_screen_summary),
                        icon = painterResource(R.drawable.round_screen_lock_portrait_24),
                        checked = state.secureScreenEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(
                                SettingsUiEvent.OnSecureScreenToggle(
                                    it
                                )
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsToggle(
                        label = stringResource(R.string.settings_haptic_feedback),
                        supportingText = stringResource(R.string.settings_haptic_feedback_desc),
                        icon = painterResource(R.drawable.round_vibration_24),
                        checked = state.hapticFeedbackEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(
                                SettingsUiEvent.OnHapticFeedbackToggle(
                                    it
                                )
                            )
                        }
                    )
                }

                // Backup Section
                SettingsSection(title = stringResource(R.string.backup_title)) {
                    SettingsItem(
                        label = stringResource(R.string.backup_export_title),
                        value = stringResource(R.string.backup_export_desc),
                        icon = painterResource(R.drawable.round_send_24),
                        onClick = { exportLauncher.launch("backtalk_backup_${System.currentTimeMillis()}.bkt") }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsItem(
                        label = stringResource(R.string.backup_import_title),
                        value = stringResource(R.string.backup_import_desc),
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
                }

                // Auto Export Section
                SettingsSection(title = stringResource(R.string.auto_export_title)) {
                    SettingsToggle(
                        label = stringResource(R.string.auto_export_title),
                        supportingText = stringResource(R.string.auto_export_summary),
                        icon = painterResource(R.drawable.round_update_24),
                        checked = state.autoExportEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && state.autoExportUri == null) folderLauncher.launch(null)
                            else viewModel.onEvent(SettingsUiEvent.OnAutoExportToggle(enabled))
                        }
                    )

                    AnimatedVisibility(visible = state.autoExportEnabled) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            val folderName = state.autoExportUri?.let {
                                DocumentFile.fromTreeUri(
                                    context,
                                    it.toUri()
                                )?.name
                            } ?: stringResource(R.string.auto_export_no_folder)
                            SettingsItem(
                                label = stringResource(R.string.auto_export_folder),
                                value = folderName,
                                icon = painterResource(R.drawable.round_folder_24),
                                onClick = { folderLauncher.launch(null) }
                            )
                            SettingsItem(
                                label = stringResource(R.string.auto_export_interval),
                                value = stringResource(state.autoExportInterval.titleResId),
                                icon = painterResource(R.drawable.round_refresh_24),
                                onClick = { showIntervalDialog.value = true }
                            )
                            SettingsToggle(
                                label = stringResource(R.string.auto_export_encrypt),
                                icon = painterResource(R.drawable.round_lock_24),
                                checked = state.autoExportEncrypted,
                                onCheckedChange = {
                                    viewModel.onEvent(
                                        SettingsUiEvent.OnAutoExportEncryptionToggle(
                                            it
                                        )
                                    )
                                    if (it && state.autoExportPassword.isNullOrBlank()) {
                                        showAutoExportPasswordDialog.value = true
                                    }
                                }
                            )
                            AnimatedVisibility(visible = state.autoExportEncrypted) {
                                SettingsItem(
                                    label = stringResource(R.string.auto_export_password),
                                    value = if (state.autoExportPassword.isNullOrBlank())
                                        stringResource(R.string.common_not_set)
                                    else stringResource(R.string.common_password_set),
                                    icon = painterResource(R.drawable.round_security_24),
                                    onClick = { showAutoExportPasswordDialog.value = true }
                                )
                            }
                        }
                    }
                }

                // Updates & About
                SettingsSection(title = stringResource(R.string.settings_information)) {
                    if (!BuildConfig.IS_FDROID) {
                        SettingsToggle(
                            label = stringResource(R.string.settings_auto_check_updates),
                            supportingText = stringResource(R.string.settings_auto_check_updates_desc),
                            icon = painterResource(R.drawable.round_update_24),
                            checked = state.autoUpdateEnabled,
                            onCheckedChange = {
                                viewModel.onEvent(
                                    SettingsUiEvent.OnAutoUpdateToggle(
                                        it
                                    )
                                )
                            }
                        )
                        SettingsItem(
                            label = stringResource(R.string.settings_check_updates_now),
                            value = stringResource(R.string.settings_check_updates_desc),
                            icon = painterResource(R.drawable.round_refresh_24),
                            onClick = {
                                context.toast(R.string.settings_checking_updates)
                                mainActivity?.checkForUpdates()
                            }
                        )
                    }
                    SettingsItem(
                        label = stringResource(R.string.settings_version),
                        value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        icon = painterResource(R.drawable.round_info_24)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsItem(
                        label = stringResource(R.string.settings_license),
                        value = stringResource(R.string.settings_license_desc),
                        icon = painterResource(R.drawable.round_info_24),
                        onClick = { navigator.push(LicenseScreen()) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsItem(
                        label = stringResource(R.string.settings_developer),
                        value = stringResource(R.string.settings_dev_name),
                        icon = painterResource(R.drawable.round_person_24),
                        onClick = {
                            if (!state.devModeEnabled) {
                                devClickCount++
                                if (devClickCount > 3) {
                                    context.toast(R.string.settings_dev_click_fine)
                                    viewModel.onEvent(SettingsUiEvent.OnDevModeToggle(true))
                                    devClickCount = 0
                                } else {
                                    context.toast(R.string.settings_dev_click)
                                }
                            } else {
                                context.toast(R.string.settings_dev_click)
                            }
                        }
                    )
                }

                // Secret Developer Section
                AnimatedVisibility(state.devModeEnabled) {
                    SettingsSection(title = stringResource(R.string.settings_secret_category)) {
                        SettingsItem(
                            label = stringResource(R.string.settings_wipe_data),
                            value = stringResource(R.string.settings_wipe_data_desc),
                            icon = painterResource(R.drawable.round_delete_sweep_24),
                            onClick = { showWipeDataDialog.value = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        SettingsToggle(
                            label = stringResource(R.string.settings_show_secret_category),
                            supportingText = stringResource(R.string.settings_show_secret_category_desc),
                            icon = painterResource(R.drawable.round_visibility_off_24),
                            checked = true,
                            onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnDevModeToggle(it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Dialogs
        if (showThemeDialog.value) {
            ThemeSelectionDialog(
                selected = state.themeMode,
                onDismiss = { showThemeDialog.value = false },
                onSelected = {
                    viewModel.onEvent(SettingsUiEvent.OnThemeModeChange(it)); showThemeDialog.value =
                    false
                }
            )
        }

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

        if (showAutoExportPasswordDialog.value) {
            AutoExportPasswordDialog(
                onDismiss = { showAutoExportPasswordDialog.value = false },
                onConfirm = { password ->
                    viewModel.onEvent(SettingsUiEvent.OnAutoExportPasswordChange(password))
                    showAutoExportPasswordDialog.value = false
                }
            )
        }

        if (showOldBackupWarning.value) {
            AlertDialog(
                onDismissRequest = {
                    showOldBackupWarning.value = false
                    viewModel.onEvent(SettingsUiEvent.ResetBackupState)
                },
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
                    Button(onClick = {
                        showOldBackupWarning.value = false
                        viewModel.onEvent(SettingsUiEvent.ResetBackupState)
                    }) {
                        Text(stringResource(R.string.common_ok))
                    }
                }
            )
        }

        if (showWipeDataDialog.value) {
            AlertDialog(
                onDismissRequest = { showWipeDataDialog.value = false },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.round_warning_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text(stringResource(R.string.settings_wipe_data_confirm_title)) },
                text = { Text(stringResource(R.string.settings_wipe_data_confirm_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showWipeDataDialog.value = false
                            viewModel.onEvent(SettingsUiEvent.WipeAppData)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.common_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWipeDataDialog.value = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }
    }

    @Composable
    private fun ThemeSelectionDialog(
        selected: ThemeMode,
        onDismiss: () -> Unit,
        onSelected: (ThemeMode) -> Unit
    ) {
        var tempSelected by remember { mutableStateOf(selected) }

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.round_brightness_6_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.settings_choose_theme)) },
            text = {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = mode == tempSelected
                        Surface(
                            selected = isSelected,
                            onClick = { tempSelected = mode },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.3f
                            )
                            else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = null)
                                Text(
                                    text = mode.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onSelected(tempSelected) }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    @Composable
    private fun AutoExportPasswordDialog(
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        var password by remember { mutableStateOf(emptyString()) }
        var passwordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.round_lock_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(text = stringResource(R.string.auto_export_password)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.auto_export_password_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.backup_enter_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(if (passwordVisible) R.drawable.round_visibility_24 else R.drawable.round_visibility_off_24),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(password) },
                    enabled = password.isNotBlank()
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    @Composable
    private fun IntervalSelectionDialog(
        selected: ExportInterval,
        onDismiss: () -> Unit,
        onSelected: (ExportInterval) -> Unit
    ) {
        var tempSelected by remember { mutableStateOf(selected) }

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.round_refresh_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.auto_export_interval)) },
            text = {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ExportInterval.entries.forEach { interval ->
                        val isSelected = interval == tempSelected
                        Surface(
                            selected = isSelected,
                            onClick = { tempSelected = interval },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.3f
                            )
                            else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = null)
                                Text(
                                    text = stringResource(interval.titleResId),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onSelected(tempSelected) }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
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
        var password by remember { mutableStateOf(emptyString()) }
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
                Text(text = stringResource(R.string.backup_export_title))
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
                                text = stringResource(R.string.settings_security_title),
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
                            label = { Text(stringResource(R.string.backup_enter_password)) },
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
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
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
                Text(text = stringResource(R.string.backup_import_strategy))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.backup_import_strategy_desc),
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
                            text = stringResource(R.string.backup_merge_data),
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
                            text = stringResource(R.string.backup_clear_and_import),
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
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
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
                    painter = painterResource(R.drawable.round_lock_24),
                    contentDescription = null,
                    tint = if (wrongPasswordError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(text = stringResource(R.string.backup_enter_password)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.backup_encrypted_prompt),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.backup_enter_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = wrongPasswordError,
                        supportingText = {
                            if (wrongPasswordError) {
                                Text(text = stringResource(R.string.backup_error_incorrect_password))
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(if (passwordVisible) R.drawable.round_visibility_24 else R.drawable.round_visibility_off_24),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(password) },
                    enabled = password.isNotBlank(),
                    colors = if (wrongPasswordError) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
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
                        contentDescription = stringResource(R.string.common_navigate_up)
                    )
                }
            },
            title = { Text(text = stringResource(R.string.settings_title)) },
            scrollBehavior = scrollBehavior
        )
    }

    @Composable
    private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(content = content)
            }
        }
    }

    @Composable
    private fun SettingsItem(
        label: String,
        value: String? = null,
        icon: Painter,
        onClick: (() -> Unit)? = null
    ) {
        ListItem(
            modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            headlineContent = { Text(label, fontWeight = FontWeight.Medium) },
            supportingContent = value?.let {
                {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            leadingContent = { SettingsIcon(icon) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }

    @Composable
    private fun SettingsToggle(
        label: String,
        supportingText: String? = null,
        icon: Painter,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        ListItem(
            modifier = Modifier.clickable { onCheckedChange(!checked) },
            headlineContent = { Text(label, fontWeight = FontWeight.Medium) },
            supportingContent = supportingText?.let { { Text(it) } },
            leadingContent = { SettingsIcon(icon) },
            trailingContent = {
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }

    @Composable
    private fun SettingsIcon(painter: Painter) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

}

