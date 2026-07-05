package off.kys.backtalk.presentation.screen.preferences.components

import android.Manifest
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.lock.AppLockManager
import off.kys.backtalk.common.lock.BiometricResult
import off.kys.backtalk.common.lock.LocalAppLockManager
import off.kys.backtalk.common.lock.rememberBiometricLauncher
import off.kys.backtalk.presentation.components.status_scaffold.ScaffoldStatus
import off.kys.backtalk.presentation.components.status_scaffold.StatusMessage
import off.kys.backtalk.presentation.components.status_scaffold.StatusScaffold
import off.kys.backtalk.presentation.event.SettingsUiEvent
import off.kys.backtalk.presentation.screen.components.changelog.ChangelogDialog
import off.kys.backtalk.presentation.state.SettingsUiState
import off.kys.backtalk.util.isSecurityEnabled
import off.kys.backtalk.util.toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onSyncClicked: () -> Unit,
    onChangelogClick: () -> Unit,
    onLicenseClicked: () -> Unit,
    onSimulateCrashClicked: () -> Unit,
    onCheckUpdates: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    val showExportDialog = remember { mutableStateOf(false) }
    val showImportStrategyDialog = remember { mutableStateOf(false) }
    val showPasswordDialog = remember { mutableStateOf(false) }
    val showIntervalDialog = remember { mutableStateOf(false) }
    val showAutoExportPasswordDialog = remember { mutableStateOf(false) }
    val showThemeDialog = remember { mutableStateOf(false) }
    val showLanguageDialog = remember { mutableStateOf(false) }
    val showOldBackupWarning = remember { mutableStateOf(false) }
    val showWipeDataDialog = remember { mutableStateOf(false) }
    val showExperimentalSyncDialog = remember { mutableStateOf(false) }
    val showReminderIntervalDialog = remember { mutableStateOf(false) }
    val showLockTimeoutDialog = remember { mutableStateOf(false) }
    val showDateFormatDialog = remember { mutableStateOf(false) }
    val showTimeFormatDialog = remember { mutableStateOf(false) }
    val showChangelogDialog = remember { mutableStateOf(false) }

    val appLockManager = LocalAppLockManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onEvent(SettingsUiEvent.OnRefreshBatteryStatus)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val biometricLauncher = rememberBiometricLauncher { result ->
        if (result is BiometricResult.Success) {
            appLockManager.setUnlocked(
                AppLockManager.Keys.SENSITIVE,
                30_000L
            )
        }
    }

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
            onEvent(SettingsUiEvent.OnAutoExportFolderChange(it))
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onEvent(SettingsUiEvent.CheckBackupEncryption(it))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onEvent(SettingsUiEvent.OnRemindersToggle(true))
        } else {
            context.toast(R.string.onboarding_permission_notifications_desc)
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
            onEvent(SettingsUiEvent.ClearError)
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            context.toast(it)
            onEvent(SettingsUiEvent.ClearSuccess)
        }
    }

    StatusScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        status = if (state.backupLoading) ScaffoldStatus.Info else ScaffoldStatus.None,
        message = if (state.backupLoading) StatusMessage.Resource(R.string.common_please_wait) else null,
        topBar = {
            SettingsTopAppBar(
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
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
                    value = stringResource(state.themeMode.titleResId),
                    icon = painterResource(R.drawable.round_brightness_6_24),
                    onClick = { showThemeDialog.value = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    label = stringResource(R.string.settings_language),
                    value = stringResource(state.appLanguage.displayNameRes),
                    icon = painterResource(R.drawable.round_language_24),
                    onClick = { showLanguageDialog.value = true }
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
                    onCheckedChange = { onEvent(SettingsUiEvent.OnDynamicColorToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_amoled_mode),
                    supportingText = stringResource(R.string.settings_amoled_mode_desc),
                    icon = painterResource(R.drawable.round_brightness_6_24),
                    checked = state.amoledMode,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnAmoledModeToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    label = stringResource(R.string.settings_date_format),
                    value = stringResource(state.dateFormat.titleResId),
                    icon = painterResource(R.drawable.round_calendar_today_24),
                    onClick = { showDateFormatDialog.value = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    label = stringResource(R.string.settings_time_format),
                    value = stringResource(state.timeFormat.titleResId),
                    icon = painterResource(R.drawable.round_calendar_clock_24),
                    onClick = { showTimeFormatDialog.value = true }
                )
            }

            // Chat Section
            SettingsSection(title = stringResource(R.string.settings_chat)) {
                SettingsToggle(
                    label = stringResource(R.string.settings_trim_messages),
                    supportingText = stringResource(R.string.settings_trim_messages_desc),
                    icon = painterResource(R.drawable.round_content_cut_24px),
                    checked = state.trimMessagesEnabled,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnTrimMessagesToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_link_preview),
                    supportingText = stringResource(R.string.settings_link_preview_desc),
                    icon = painterResource(R.drawable.round_preview_24),
                    checked = state.linkPreviewEnabled,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnLinkPreviewToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_send_with_enter),
                    supportingText = stringResource(R.string.settings_send_with_enter_desc),
                    icon = painterResource(R.drawable.round_send_24),
                    checked = state.sendWithEnter,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnSendWithEnterToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_remove_image_metadata),
                    supportingText = stringResource(R.string.settings_remove_image_metadata_desc),
                    icon = painterResource(R.drawable.round_description_24),
                    checked = state.removeImageMetadataEnabled,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnRemoveImageMetadataToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_smart_pointing_images),
                    supportingText = stringResource(R.string.settings_smart_pointing_images_desc),
                    icon = painterResource(R.drawable.round_arrow_or_edge_24),
                    checked = state.smartImagePointingEnabled,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnSmartImagePointingToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_show_tags_bar),
                    supportingText = stringResource(R.string.settings_show_tags_bar_desc),
                    icon = painterResource(R.drawable.round_tag_24),
                    checked = state.showTagsBar,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnShowTagsBarToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_disable_context_menu),
                    supportingText = stringResource(R.string.settings_disable_context_menu_desc),
                    icon = painterResource(R.drawable.round_visibility_off_24),
                    checked = state.disableContextMenuOnLongClick,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnDisableContextMenuToggle(it)) }
                )
            }

            // Reminders Section
            SettingsSection(title = stringResource(R.string.settings_reminders)) {
                SettingsToggle(
                    label = stringResource(R.string.settings_reminders),
                    supportingText = stringResource(R.string.settings_reminders_desc),
                    icon = painterResource(R.drawable.round_notifications_24),
                    checked = state.remindersEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onEvent(SettingsUiEvent.OnRemindersToggle(enabled))
                        }
                    }
                )
                AnimatedVisibility(visible = state.remindersEnabled) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        SettingsItem(
                            label = stringResource(R.string.settings_reminder_interval),
                            value = stringResource(state.reminderInterval.titleResId),
                            icon = painterResource(R.drawable.round_refresh_24),
                            onClick = { showReminderIntervalDialog.value = true }
                        )
                    }
                }
            }

            // Security Section
            SettingsSection(title = stringResource(R.string.settings_privacy_security)) {
                if (context.isSecurityEnabled()) {
                    SettingsToggle(
                        label = stringResource(R.string.settings_enable_app_lock),
                        supportingText = stringResource(R.string.settings_app_lock_desc),
                        icon = painterResource(R.drawable.round_lock_24),
                        checked = state.lockEnabled,
                        onCheckedChange = { enabled ->
                            if (state.lockEnabled && !appLockManager.isUnlocked(AppLockManager.Keys.SENSITIVE)) {
                                biometricLauncher()
                                // We don't toggle yet, we wait for user to authenticate and click again, 
                                // or we could handle it better. 
                                // To keep it simple: if unlocked, toggle. If not, prompt.
                            } else {
                                onEvent(SettingsUiEvent.OnLockToggle(enabled))
                            }
                        }
                    )
                    AnimatedVisibility(visible = state.lockEnabled) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            val timeoutLabel = when (state.lockTimeoutMillis) {
                                0L -> stringResource(R.string.lock_timeout_immediately)
                                15_000L -> stringResource(R.string.lock_timeout_15s)
                                30_000L -> stringResource(R.string.lock_timeout_30s)
                                60_000L -> stringResource(R.string.lock_timeout_1m)
                                300_000L -> stringResource(R.string.lock_timeout_5m)
                                600_000L -> stringResource(R.string.lock_timeout_10m)
                                1_800_000L -> stringResource(R.string.lock_timeout_30m)
                                else -> state.lockTimeoutMillis.toString()
                            }
                            SettingsItem(
                                label = stringResource(R.string.settings_lock_timeout),
                                value = timeoutLabel,
                                icon = painterResource(R.drawable.round_access_alarm_24),
                                onClick = { showLockTimeoutDialog.value = true }
                            )
                            AnimatedVisibility(visible = state.lockTimeoutMillis != 0L) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                SettingsToggle(
                                    label = stringResource(R.string.settings_lock_on_screen_off),
                                    supportingText = stringResource(R.string.settings_lock_on_screen_off_desc),
                                    icon = painterResource(R.drawable.round_screen_lock_portrait_24),
                                    checked = state.lockOnScreenOff,
                                    onCheckedChange = {
                                        onEvent(
                                            SettingsUiEvent.OnLockOnScreenOffToggle(
                                                it
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
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
                    onCheckedChange = { onEvent(SettingsUiEvent.OnSecureScreenToggle(it)) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggle(
                    label = stringResource(R.string.settings_external_link_warning),
                    supportingText = stringResource(R.string.settings_external_link_warning_desc),
                    icon = painterResource(R.drawable.round_warning_24),
                    checked = state.externalLinkWarningEnabled,
                    onCheckedChange = { onEvent(SettingsUiEvent.OnExternalLinkWarningToggle(it)) }
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
                    onCheckedChange = { onEvent(SettingsUiEvent.OnHapticFeedbackToggle(it)) }
                )
            }

            // Backup Section
            SettingsSection(title = stringResource(R.string.backup_title)) {
                SettingsItem(
                    label = stringResource(R.string.backup_export_title),
                    supportingText = stringResource(R.string.backup_export_desc),
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
                    supportingText = stringResource(R.string.backup_import_desc),
                    icon = painterResource(R.drawable.round_reply_24),
                    onClick = {
                        importLauncher.launch(
                            arrayOf("application/json", "application/octet-stream", "*/*")
                        )
                    }
                )
            }

            // Sync Section
            SettingsSection(title = stringResource(R.string.sync_title)) {
                SettingsItem(
                    label = stringResource(R.string.sync_title),
                    supportingText = stringResource(R.string.sync_summary),
                    icon = painterResource(R.drawable.round_refresh_24),
                    onClick = onSyncClicked
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
                        else onEvent(SettingsUiEvent.OnAutoExportToggle(enabled))
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
                            DocumentFile.fromTreeUri(context, it.toUri())?.name
                        } ?: stringResource(R.string.auto_export_no_folder)
                        SettingsItem(
                            label = stringResource(R.string.auto_export_folder),
                            value = folderName,
                            icon = painterResource(R.drawable.round_folder_24),
                            onClick = { folderLauncher.launch(null) }
                        )
                        SettingsItem(
                            label = stringResource(R.string.auto_export_interval),
                            value = stringResource(state.autoRepeatFrequency.titleResId),
                            icon = painterResource(R.drawable.round_refresh_24),
                            onClick = { showIntervalDialog.value = true }
                        )
                        SettingsToggle(
                            label = stringResource(R.string.auto_export_encrypt),
                            icon = painterResource(R.drawable.round_lock_24),
                            checked = state.autoExportEncrypted,
                            onCheckedChange = {
                                onEvent(SettingsUiEvent.OnAutoExportEncryptionToggle(it))
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

            // Troubleshooting Section
            SettingsSection(title = stringResource(R.string.settings_troubleshooting)) {
                val batteryStatus = if (state.isIgnoringBatteryOptimizations) {
                    stringResource(R.string.settings_battery_optimization_ignored)
                } else {
                    stringResource(R.string.settings_battery_optimization_not_ignored)
                }
                SettingsItem(
                    label = stringResource(R.string.settings_disable_battery_optimization),
                    supportingText = stringResource(R.string.settings_disable_battery_optimization_desc),
                    value = batteryStatus,
                    icon = painterResource(R.drawable.round_phone_android_24),
                    onClick = { onEvent(SettingsUiEvent.OnDisableBatteryOptimization) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    label = stringResource(R.string.settings_dont_kill_my_app),
                    supportingText = stringResource(R.string.settings_dont_kill_my_app_desc),
                    icon = painterResource(R.drawable.round_warning_24),
                    onClick = { onEvent(SettingsUiEvent.OnOpenDontKillMyApp) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    label = stringResource(R.string.settings_clear_cache),
                    supportingText = stringResource(R.string.settings_clear_cache_desc),
                    value = state.cacheSize,
                    icon = painterResource(R.drawable.round_cleaning_services_24),
                    onClick = { onEvent(SettingsUiEvent.OnClearCache) }
                )
            }

            // Updates & About
            SettingsSection(title = stringResource(R.string.settings_information)) {
                if (!BuildConfig.IS_FDROID) {
                    SettingsToggle(
                        label = stringResource(R.string.settings_auto_check_updates),
                        supportingText = stringResource(R.string.settings_auto_check_updates_desc),
                        icon = painterResource(R.drawable.round_update_24),
                        checked = state.autoUpdateEnabled,
                        onCheckedChange = { onEvent(SettingsUiEvent.OnAutoUpdateToggle(it)) }
                    )
                    SettingsItem(
                        label = stringResource(R.string.settings_check_updates_now),
                        supportingText = stringResource(R.string.settings_check_updates_desc),
                        icon = painterResource(R.drawable.round_refresh_24),
                        onClick = {
                            context.toast(R.string.settings_checking_updates)
                            onCheckUpdates()
                        }
                    )
                }
                SettingsItem(
                    label = stringResource(R.string.settings_version),
                    value = stringResource(
                        R.string.settings_version_current,
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    ),
                    icon = painterResource(R.drawable.round_info_24)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    label = stringResource(R.string.settings_changelog),
                    supportingText = stringResource(R.string.settings_changelog_desc),
                    icon = painterResource(R.drawable.round_update_24),
                    onClick = onChangelogClick,
                    onLongClick = { showChangelogDialog.value = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsItem(
                    label = stringResource(R.string.settings_license),
                    supportingText = stringResource(R.string.settings_license_desc),
                    icon = painterResource(R.drawable.round_code_xml_24),
                    onClick = onLicenseClicked
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
                                onEvent(SettingsUiEvent.OnDevModeToggle(true))
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

            // Developer Options
            AnimatedVisibility(state.devModeEnabled) {
                SettingsSection(title = stringResource(R.string.settings_secret_category)) {
                    SettingsToggle(
                        label = stringResource(R.string.settings_show_secret_category),
                        supportingText = stringResource(R.string.settings_show_secret_category_desc),
                        icon = painterResource(R.drawable.round_visibility_off_24),
                        checked = true,
                        onCheckedChange = { onEvent(SettingsUiEvent.OnDevModeToggle(it)) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsToggle(
                        label = stringResource(R.string.settings_stay_awake),
                        supportingText = stringResource(R.string.settings_stay_awake_desc),
                        icon = painterResource(R.drawable.round_visibility_24),
                        checked = state.keepScreenOn,
                        onCheckedChange = { onEvent(SettingsUiEvent.OnKeepScreenOnToggle(it)) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsItem(
                        label = stringResource(R.string.settings_wipe_data),
                        supportingText = stringResource(R.string.settings_wipe_data_desc),
                        icon = painterResource(R.drawable.round_delete_sweep_24),
                        onClick = {
                            if (state.lockEnabled && !appLockManager.isUnlocked(AppLockManager.Keys.SENSITIVE)) {
                                biometricLauncher()
                            } else {
                                showWipeDataDialog.value = true
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsItem(
                        label = stringResource(R.string.settings_simulate_crash),
                        supportingText = stringResource(R.string.settings_simulate_crash_desc),
                        icon = painterResource(R.drawable.round_warning_24),
                        onClick = onSimulateCrashClicked
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Extracted & External Custom Dialog Handlers
    if (showThemeDialog.value) {
        ThemeSelectionDialog(
            selected = state.themeMode,
            onDismiss = { showThemeDialog.value = false },
            onSelected = {
                onEvent(SettingsUiEvent.OnThemeModeChange(it))
                showThemeDialog.value = false
            }
        )
    }

    if (showLanguageDialog.value) {
        LanguageSelectionDialog(
            selected = state.appLanguage,
            onDismiss = { showLanguageDialog.value = false },
            onSelected = {
                onEvent(SettingsUiEvent.OnLanguageChange(it))
                showLanguageDialog.value = false
            }
        )
    }

    if (showExportDialog.value) {
        ExportDialog(
            onDismiss = {
                showExportDialog.value = false
                selectedUri?.let { uri ->
                    runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) }
                }
                selectedUri = null
            },
            onConfirm = { exportPassword ->
                showExportDialog.value = false
                selectedUri?.let { uri ->
                    onEvent(SettingsUiEvent.ExportBackup(uri, exportPassword))
                }
                selectedUri = null
            }
        )
    }

    if (showImportStrategyDialog.value) {
        ImportStrategyDialog(
            onDismiss = {
                showImportStrategyDialog.value = false
                onEvent(SettingsUiEvent.ResetBackupState)
            },
            onConfirm = { clearAndImport ->
                showImportStrategyDialog.value = false
                isImporting = clearAndImport
                if (state.isBackupEncrypted == true) {
                    showPasswordDialog.value = true
                } else {
                    state.selectedBackupUri?.let { uri ->
                        onEvent(SettingsUiEvent.ImportBackup(uri, null, clearAndImport))
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
                onEvent(SettingsUiEvent.ResetBackupState)
            },
            onConfirm = { enteredPassword ->
                showPasswordDialog.value = false
                state.selectedBackupUri?.let { uri ->
                    onEvent(
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
            title = stringResource(R.string.auto_export_interval),
            selected = state.autoRepeatFrequency,
            onDismiss = { showIntervalDialog.value = false },
            onSelected = {
                onEvent(SettingsUiEvent.OnAutoExportIntervalChange(it))
                showIntervalDialog.value = false
            },
        )
    }

    if (showAutoExportPasswordDialog.value) {
        AutoExportPasswordDialog(
            onDismiss = { showAutoExportPasswordDialog.value = false },
            onConfirm = { password ->
                onEvent(SettingsUiEvent.OnAutoExportPasswordChange(password))
                showAutoExportPasswordDialog.value = false
            }
        )
    }

    if (showOldBackupWarning.value) {
        OldBackupWarningDialog(
            onDismiss = {
                showOldBackupWarning.value = false
                onEvent(SettingsUiEvent.ResetBackupState)
            }
        )
    }

    if (showWipeDataDialog.value) {
        WipeDataDialog(
            onDismiss = { showWipeDataDialog.value = false },
            onConfirm = {
                showWipeDataDialog.value = false
                onEvent(SettingsUiEvent.WipeAppData)
            }
        )
    }

    if (showExperimentalSyncDialog.value) {
        ExperimentalSyncDialog(
            onDismiss = { showExperimentalSyncDialog.value = false }
        )
    }

    if (showReminderIntervalDialog.value) {
        IntervalSelectionDialog(
            title = stringResource(R.string.settings_reminder_interval),
            selected = state.reminderInterval,
            onDismiss = { showReminderIntervalDialog.value = false },
            onSelected = {
                onEvent(SettingsUiEvent.OnReminderIntervalChange(it))
                showReminderIntervalDialog.value = false
            },
        )
    }

    if (showLockTimeoutDialog.value) {
        LockTimeoutSelectionDialog(
            selectedTimeout = state.lockTimeoutMillis,
            onDismiss = { showLockTimeoutDialog.value = false },
            onSelected = {
                onEvent(SettingsUiEvent.OnLockTimeoutChange(it))
                showLockTimeoutDialog.value = false
            }
        )
    }

    if (showDateFormatDialog.value) {
        DateFormatSelectionDialog(
            selectedFormat = state.dateFormat,
            customPattern = state.customDateFormat,
            onFormatSelected = { onEvent(SettingsUiEvent.OnDateFormatChange(it)) },
            onCustomPatternChanged = { onEvent(SettingsUiEvent.OnCustomDateFormatChange(it)) },
            onDismiss = { showDateFormatDialog.value = false }
        )
    }

    if (showTimeFormatDialog.value) {
        TimeFormatSelectionDialog(
            selectedFormat = state.timeFormat,
            onFormatSelected = { onEvent(SettingsUiEvent.OnTimeFormatChange(it)) },
            onDismiss = { showTimeFormatDialog.value = false }
        )
    }

    if (showChangelogDialog.value) {
        ChangelogDialog(
            onDismiss = { showChangelogDialog.value = false }
        )
    }
}
