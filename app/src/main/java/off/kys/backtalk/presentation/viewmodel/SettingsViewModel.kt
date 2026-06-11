package off.kys.backtalk.presentation.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.AppLanguage
import off.kys.backtalk.common.RepeatFrequency
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.domain.use_case.ImportBackup
import off.kys.backtalk.domain.use_case.WipeAppData
import off.kys.backtalk.domain.use_case_bundle.BackupUseCases
import off.kys.backtalk.presentation.event.SettingsUiEvent
import off.kys.backtalk.presentation.state.SettingsUiState
import off.kys.backtalk.util.WorkScheduler
import java.security.GeneralSecurityException
import javax.crypto.AEADBadTagException
import javax.crypto.BadPaddingException

/**
 * ViewModel for the Settings screen.
 */
class SettingsViewModel(
    private val application: Application,
    private val preferences: BacktalkPreferences,
    private val backupUseCases: BackupUseCases,
    private val wipeAppData: WipeAppData
) : AndroidViewModel(application) {

    private val context: Context by lazy { application.applicationContext }

    private val _state = MutableStateFlow(
        SettingsUiState(
            themeMode = preferences.themeMode,
            appLanguage = AppLanguage.fromTag(
                AppCompatDelegate.getApplicationLocales().toLanguageTags()
            ),
            dynamicColorEnabled = preferences.dynamicColorEnabled,
            lockEnabled = preferences.lockEnabled,
            lockOnScreenOff = preferences.lockOnScreenOff,
            lockTimeoutMillis = preferences.lockTimeoutMillis,
            secureScreenEnabled = preferences.secureScreenEnabled,
            autoUpdateEnabled = preferences.autoUpdateEnabled,
            dateFormat = preferences.dateFormat,
            timeFormat = preferences.timeFormat,
            customDateFormat = preferences.customDateFormat ?: "MMM d, yyyy",
            autoExportEnabled = preferences.autoExportEnabled,
            autoRepeatFrequency = preferences.autoRepeatFrequency,
            autoExportUri = preferences.autoExportUri,
            autoExportEncrypted = preferences.autoExportEncrypted,
            autoExportPassword = preferences.autoExportPassword,
            remindersEnabled = preferences.remindersEnabled,
            reminderInterval = preferences.reminderInterval,
            hapticFeedbackEnabled = preferences.hapticFeedbackEnabled,
            keepScreenOn = preferences.keepScreenOn,
            devModeEnabled = preferences.devModeEnabled,
            externalLinkWarningEnabled = preferences.externalLinkWarningEnabled,
            trimMessagesEnabled = preferences.trimMessagesEnabled,
            linkPreviewEnabled = preferences.linkPreviewEnabled,
            sendWithEnter = preferences.sendWithEnter,
            removeImageMetadataEnabled = preferences.removeImageMetadataEnabled,
            smartImagePointingEnabled = preferences.smartImagePointingEnabled,
            showTagsBar = preferences.showTagsBar,
            lastSeenChangelogVersion = preferences.lastSeenChangelogVersion.orEmpty(),
            isIgnoringBatteryOptimizations = (application.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .isIgnoringBatteryOptimizations(application.packageName)
        )
    )
    val state = _state.asStateFlow()

    fun onEvent(event: SettingsUiEvent) = when (event) {
        is SettingsUiEvent.OnThemeModeChange -> onThemeModeChange(event.themeMode)
        is SettingsUiEvent.OnLanguageChange -> onLanguageChange(event.language)
        is SettingsUiEvent.OnDateFormatChange -> onDateFormatChange(event.dateFormat)
        is SettingsUiEvent.OnTimeFormatChange -> onTimeFormatChange(event.timeFormat)
        is SettingsUiEvent.OnCustomDateFormatChange -> onCustomDateFormatChange(event.pattern)
        is SettingsUiEvent.OnDynamicColorToggle -> onDynamicColorToggle(event.enabled)
        is SettingsUiEvent.OnLockToggle -> onLockToggle(event.enabled)
        is SettingsUiEvent.OnLockOnScreenOffToggle -> onLockOnScreenOffToggle(event.enabled)
        is SettingsUiEvent.OnLockTimeoutChange -> onLockTimeoutChange(event.timeoutMillis)
        is SettingsUiEvent.OnSecureScreenToggle -> onSecureScreenToggle(event.enabled)
        is SettingsUiEvent.OnAutoUpdateToggle -> onAutoUpdateToggle(event.enabled)
        is SettingsUiEvent.OnAutoExportToggle -> onAutoExportToggle(event.enabled)
        is SettingsUiEvent.OnAutoExportIntervalChange -> onAutoExportIntervalChange(event.interval)
        is SettingsUiEvent.OnAutoExportFolderChange -> onAutoExportFolderChange(event.uri)
        is SettingsUiEvent.OnAutoExportEncryptionToggle -> onAutoExportEncryptionToggle(event.enabled)
        is SettingsUiEvent.OnAutoExportPasswordChange -> onAutoExportPasswordChange(event.password)
        is SettingsUiEvent.OnRemindersToggle -> onRemindersToggle(event.enabled)
        is SettingsUiEvent.OnReminderIntervalChange -> onReminderIntervalChange(event.interval)
        is SettingsUiEvent.OnHapticFeedbackToggle -> onHapticFeedbackToggle(event.enabled)
        is SettingsUiEvent.OnKeepScreenOnToggle -> onKeepScreenOnToggle(event.enabled)
        is SettingsUiEvent.OnDevModeToggle -> onDevModeToggle(event.enabled)
        is SettingsUiEvent.OnExternalLinkWarningToggle -> onExternalLinkWarningToggle(event.enabled)
        is SettingsUiEvent.OnTrimMessagesToggle -> onTrimMessagesToggle(event.enabled)
        is SettingsUiEvent.OnLinkPreviewToggle -> onLinkPreviewToggle(event.enabled)
        is SettingsUiEvent.OnSendWithEnterToggle -> onSendWithEnterToggle(event.enabled)
        is SettingsUiEvent.OnRemoveImageMetadataToggle -> onRemoveImageMetadataToggle(event.enabled)
        is SettingsUiEvent.OnSmartImagePointingToggle -> onSmartImagePointingToggle(event.enabled)
        is SettingsUiEvent.OnShowTagsBarToggle -> onShowTagsBarToggle(event.enabled)
        is SettingsUiEvent.OnChangelogVersionUpdate -> onChangelogVersionUpdate(event.version)
        SettingsUiEvent.OnDisableBatteryOptimization -> onDisableBatteryOptimization()
        SettingsUiEvent.OnOpenDontKillMyApp -> onOpenDontKillMyApp()
        SettingsUiEvent.OnRefreshBatteryStatus -> onRefreshBatteryStatus()
        is SettingsUiEvent.ExportBackup -> exportBackup(event.uri, event.password)
        is SettingsUiEvent.CheckBackupEncryption -> checkBackupEncryption(event.uri)
        is SettingsUiEvent.ImportBackup -> importBackup(
            event.uri,
            event.password,
            event.clearExisting
        )

        SettingsUiEvent.ClearError -> clearError()
        SettingsUiEvent.ClearSuccess -> clearSuccess()
        SettingsUiEvent.ResetBackupState -> resetBackupState()
        SettingsUiEvent.WipeAppData -> onWipeAppData()
    }

    private fun onThemeModeChange(themeMode: ThemeMode) {
        preferences.themeMode = themeMode
        _state.update { it.copy(themeMode = themeMode) }
    }

    private fun onLanguageChange(language: AppLanguage) {
        val appLocale: LocaleListCompat = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
        _state.update { it.copy(appLanguage = language) }
    }

    private fun onDateFormatChange(dateFormat: off.kys.backtalk.common.AppDateFormat) {
        preferences.dateFormat = dateFormat
        _state.update { it.copy(dateFormat = dateFormat) }
    }

    private fun onTimeFormatChange(timeFormat: off.kys.backtalk.common.AppTimeFormat) {
        preferences.timeFormat = timeFormat
        _state.update { it.copy(timeFormat = timeFormat) }
    }

    private fun onCustomDateFormatChange(pattern: String) {
        preferences.customDateFormat = pattern
        _state.update { it.copy(customDateFormat = pattern) }
    }

    private fun onDynamicColorToggle(enabled: Boolean) {
        preferences.dynamicColorEnabled = enabled
        _state.update { it.copy(dynamicColorEnabled = enabled) }
    }

    private fun onLockToggle(enabled: Boolean) {
        preferences.lockEnabled = enabled
        _state.update { it.copy(lockEnabled = enabled) }
    }

    private fun onLockOnScreenOffToggle(enabled: Boolean) {
        preferences.lockOnScreenOff = enabled
        _state.update { it.copy(lockOnScreenOff = enabled) }
    }

    private fun onLockTimeoutChange(timeoutMillis: Long) {
        preferences.lockTimeoutMillis = timeoutMillis
        _state.update { it.copy(lockTimeoutMillis = timeoutMillis) }
    }

    private fun onSecureScreenToggle(enabled: Boolean) {
        preferences.secureScreenEnabled = enabled
        _state.update { it.copy(secureScreenEnabled = enabled) }
    }

    private fun onAutoUpdateToggle(enabled: Boolean) {
        preferences.autoUpdateEnabled = enabled
        _state.update { it.copy(autoUpdateEnabled = enabled) }
        WorkScheduler.scheduleAutoUpdate(context, preferences, forceReplace = true)
    }

    private fun onAutoExportToggle(enabled: Boolean) {
        preferences.autoExportEnabled = enabled
        _state.update { it.copy(autoExportEnabled = enabled) }
        WorkScheduler.scheduleAutoExport(context, preferences, forceReplace = true)
    }

    private fun onAutoExportIntervalChange(interval: RepeatFrequency) {
        preferences.autoRepeatFrequency = interval
        _state.update { it.copy(autoRepeatFrequency = interval) }
        WorkScheduler.scheduleAutoExport(context, preferences, forceReplace = true)
    }

    private fun onAutoExportFolderChange(uri: Uri) {
        val flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        context.contentResolver.takePersistableUriPermission(uri, flags)
        
        val uriString = uri.toString()
        preferences.autoExportUri = uriString
        _state.update { it.copy(autoExportUri = uriString) }
        
        WorkScheduler.scheduleAutoExport(context, preferences, forceReplace = true)
        if (!preferences.autoExportEnabled) {
             _state.update { it.copy(autoExportEnabled = true) }
             preferences.autoExportEnabled = true
        }
    }

    private fun onAutoExportEncryptionToggle(enabled: Boolean) {
        preferences.autoExportEncrypted = enabled
        _state.update { it.copy(autoExportEncrypted = enabled) }
    }

    private fun onAutoExportPasswordChange(password: String?) {
        preferences.autoExportPassword = password
        _state.update { it.copy(autoExportPassword = password) }
    }

    private fun onRemindersToggle(enabled: Boolean) {
        preferences.remindersEnabled = enabled
        _state.update { it.copy(remindersEnabled = enabled) }
        WorkScheduler.scheduleReminders(context, preferences, forceReplace = true)
    }

    private fun onReminderIntervalChange(interval: RepeatFrequency) {
        preferences.reminderInterval = interval
        _state.update { it.copy(reminderInterval = interval) }
        WorkScheduler.scheduleReminders(context, preferences, forceReplace = true)
    }

    private fun onHapticFeedbackToggle(enabled: Boolean) {
        preferences.hapticFeedbackEnabled = enabled
        _state.update { it.copy(hapticFeedbackEnabled = enabled) }
    }

    private fun onKeepScreenOnToggle(enabled: Boolean) {
        preferences.keepScreenOn = enabled
        _state.update { it.copy(keepScreenOn = enabled) }
    }

    private fun onDevModeToggle(enabled: Boolean) {
        preferences.devModeEnabled = enabled
        _state.update { it.copy(devModeEnabled = enabled) }
    }

    private fun onExternalLinkWarningToggle(enabled: Boolean) {
        preferences.externalLinkWarningEnabled = enabled
        _state.update { it.copy(externalLinkWarningEnabled = enabled) }
    }

    private fun onTrimMessagesToggle(enabled: Boolean) {
        preferences.trimMessagesEnabled = enabled
        _state.update { it.copy(trimMessagesEnabled = enabled) }
    }

    private fun onLinkPreviewToggle(enabled: Boolean) {
        preferences.linkPreviewEnabled = enabled
        _state.update { it.copy(linkPreviewEnabled = enabled) }
    }

    private fun onSendWithEnterToggle(enabled: Boolean) {
        preferences.sendWithEnter = enabled
        _state.update { it.copy(sendWithEnter = enabled) }
    }

    private fun onRemoveImageMetadataToggle(enabled: Boolean) {
        preferences.removeImageMetadataEnabled = enabled
        _state.update { it.copy(removeImageMetadataEnabled = enabled) }
    }

    private fun onSmartImagePointingToggle(enabled: Boolean) {
        preferences.smartImagePointingEnabled = enabled
        _state.update { it.copy(smartImagePointingEnabled = enabled) }
    }

    private fun onShowTagsBarToggle(enabled: Boolean) {
        preferences.showTagsBar = enabled
        _state.update { it.copy(showTagsBar = enabled) }
    }

    private fun onChangelogVersionUpdate(version: String) {
        preferences.lastSeenChangelogVersion = version
        _state.update { it.copy(lastSeenChangelogVersion = version) }
    }

    @SuppressLint("BatteryLife")
    private fun onDisableBatteryOptimization() {
        val packageName = context.packageName
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private fun onOpenDontKillMyApp() {
        val intent = Intent(Intent.ACTION_VIEW, "https://dontkillmyapp.com/".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun onRefreshBatteryStatus() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoring = pm.isIgnoringBatteryOptimizations(context.packageName)
        _state.update { it.copy(isIgnoringBatteryOptimizations = isIgnoring) }
    }

    private fun onWipeAppData() {
        viewModelScope.launch {
            _state.update { it.copy(backupLoading = true) }
            wipeAppData()
            // After wipe, state needs to be refreshed or app restarted.
            // For now, update state with defaults from preferences (which were cleared)
            _state.update {
                SettingsUiState(
                    themeMode = preferences.themeMode,
                    dynamicColorEnabled = preferences.dynamicColorEnabled,
                    devModeEnabled = preferences.devModeEnabled
                )
            }
        }
    }

    private fun exportBackup(uri: Uri, password: String?) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    backupLoading = true,
                    error = null,
                    successMessage = null
                )
            }
            backupUseCases.exportBackup(uri, password)
                .onSuccess {
                    _state.update {
                        it.copy(
                            backupLoading = false,
                            successMessage = context.getString(R.string.backup_export_success)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            backupLoading = false,
                            error = error.message
                                ?: context.getString(R.string.backup_export_failed)
                        )
                    }
                }
        }
    }

    private fun checkBackupEncryption(uri: Uri) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    backupLoading = true,
                    error = null,
                    isBackupEncrypted = null,
                    selectedBackupUri = uri
                )
            }
            backupUseCases.isEncrypted(uri)
                .onSuccess { isEncrypted ->
                    _state.update {
                        it.copy(
                            backupLoading = false,
                            isBackupEncrypted = isEncrypted
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            backupLoading = false,
                            error = error.message
                                ?: context.getString(R.string.backup_check_failed)
                        )
                    }
                }
        }
    }

    private fun importBackup(uri: Uri, password: String?, clearExisting: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    backupLoading = true,
                    error = null,
                    successMessage = null,
                    wrongPasswordError = false
                )
            }
            backupUseCases.importBackup(uri, password, clearExisting)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            backupLoading = false,
                            successMessage = context.getString(R.string.backup_import_success),
                            themeMode = preferences.themeMode,
                            dynamicColorEnabled = preferences.dynamicColorEnabled,
                            lockEnabled = preferences.lockEnabled,
                            lockOnScreenOff = preferences.lockOnScreenOff,
                            secureScreenEnabled = preferences.secureScreenEnabled,
                            autoUpdateEnabled = preferences.autoUpdateEnabled,
                            isBackupEncrypted = null,
                            selectedBackupUri = null,
                            showOldBackupWarning = result is ImportBackup.ImportResult.SuccessWithWarning
                        )
                    }
                }
                .onFailure { error ->
                    val isWrongPassword = when (error) {
                        is AEADBadTagException , is BadPaddingException -> true
                        is GeneralSecurityException -> {
                            var cause: Throwable? = error.cause
                            var match = false
                            while (cause != null) {
                                if (cause is AEADBadTagException || cause is BadPaddingException) {
                                    match = true
                                    break
                                }
                                cause = cause.cause
                            }
                            match
                        }
                        else -> false
                    }

                    _state.update {
                        it.copy(
                            backupLoading = false,
                            error = if (isWrongPassword) null else (error.message
                                ?: context.getString(R.string.backup_import_failed)),
                            wrongPasswordError = isWrongPassword
                        )
                    }
                }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }

    private fun resetBackupState() {
        _state.update {
            it.copy(
                isBackupEncrypted = null,
                selectedBackupUri = null,
                wrongPasswordError = false,
                showOldBackupWarning = false
            )
        }
    }
}
