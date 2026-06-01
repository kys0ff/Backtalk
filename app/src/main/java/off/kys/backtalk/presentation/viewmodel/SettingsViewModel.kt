package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.AppLanguage
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.worker.AutoExportWorker
import off.kys.backtalk.data.worker.ReminderWorker
import off.kys.backtalk.domain.use_case.ImportBackup
import off.kys.backtalk.domain.use_case.WipeAppData
import off.kys.backtalk.domain.use_case_bundle.BackupUseCases
import off.kys.backtalk.presentation.event.SettingsUiEvent
import off.kys.backtalk.presentation.state.SettingsUiState
import java.util.concurrent.TimeUnit
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
            secureScreenEnabled = preferences.secureScreenEnabled,
            autoUpdateEnabled = preferences.autoUpdateEnabled,
            autoExportEnabled = preferences.autoExportEnabled,
            autoExportInterval = preferences.autoExportInterval,
            autoExportUri = preferences.autoExportUri,
            autoExportEncrypted = preferences.autoExportEncrypted,
            autoExportPassword = preferences.autoExportPassword,
            remindersEnabled = preferences.remindersEnabled,
            reminderInterval = preferences.reminderInterval,
            smartReminderIntensity = preferences.smartReminderIntensity,
            hapticFeedbackEnabled = preferences.hapticFeedbackEnabled,
            keepScreenOn = preferences.keepScreenOn,
            devModeEnabled = preferences.devModeEnabled,
            externalLinkWarningEnabled = preferences.externalLinkWarningEnabled,
            trimMessagesEnabled = preferences.trimMessagesEnabled,
            removeImageMetadataEnabled = preferences.removeImageMetadataEnabled,
            smartImagePointingEnabled = preferences.smartImagePointingEnabled
        )
    )
    val state = _state.asStateFlow()

    fun onEvent(event: SettingsUiEvent) = when (event) {
        is SettingsUiEvent.OnThemeModeChange -> onThemeModeChange(event.themeMode)
        is SettingsUiEvent.OnLanguageChange -> onLanguageChange(event.language)
        is SettingsUiEvent.OnDynamicColorToggle -> onDynamicColorToggle(event.enabled)
        is SettingsUiEvent.OnLockToggle -> onLockToggle(event.enabled)
        is SettingsUiEvent.OnSecureScreenToggle -> onSecureScreenToggle(event.enabled)
        is SettingsUiEvent.OnAutoUpdateToggle -> onAutoUpdateToggle(event.enabled)
        is SettingsUiEvent.OnAutoExportToggle -> onAutoExportToggle(event.enabled)
        is SettingsUiEvent.OnAutoExportIntervalChange -> onAutoExportIntervalChange(event.interval)
        is SettingsUiEvent.OnAutoExportFolderChange -> onAutoExportFolderChange(event.uri)
        is SettingsUiEvent.OnAutoExportEncryptionToggle -> onAutoExportEncryptionToggle(event.enabled)
        is SettingsUiEvent.OnAutoExportPasswordChange -> onAutoExportPasswordChange(event.password)
        is SettingsUiEvent.OnRemindersToggle -> onRemindersToggle(event.enabled)
        is SettingsUiEvent.OnReminderIntervalChange -> onReminderIntervalChange(event.interval)
        is SettingsUiEvent.OnSmartIntensityChange -> onSmartIntensityChange(event.intensity)
        is SettingsUiEvent.OnHapticFeedbackToggle -> onHapticFeedbackToggle(event.enabled)
        is SettingsUiEvent.OnKeepScreenOnToggle -> onKeepScreenOnToggle(event.enabled)
        is SettingsUiEvent.OnDevModeToggle -> onDevModeToggle(event.enabled)
        is SettingsUiEvent.OnExternalLinkWarningToggle -> onExternalLinkWarningToggle(event.enabled)
        is SettingsUiEvent.OnTrimMessagesToggle -> onTrimMessagesToggle(event.enabled)
        is SettingsUiEvent.OnRemoveImageMetadataToggle -> onRemoveImageMetadataToggle(event.enabled)
        is SettingsUiEvent.OnSmartImagePointingToggle -> onSmartImagePointingToggle(event.enabled)
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

    private fun onDynamicColorToggle(enabled: Boolean) {
        preferences.dynamicColorEnabled = enabled
        _state.update { it.copy(dynamicColorEnabled = enabled) }
    }

    private fun onLockToggle(enabled: Boolean) {
        preferences.lockEnabled = enabled
        _state.update { it.copy(lockEnabled = enabled) }
    }

    private fun onSecureScreenToggle(enabled: Boolean) {
        preferences.secureScreenEnabled = enabled
        _state.update { it.copy(secureScreenEnabled = enabled) }
    }

    private fun onAutoUpdateToggle(enabled: Boolean) {
        preferences.autoUpdateEnabled = enabled
        _state.update { it.copy(autoUpdateEnabled = enabled) }
    }

    private fun onAutoExportToggle(enabled: Boolean) {
        preferences.autoExportEnabled = enabled
        _state.update { it.copy(autoExportEnabled = enabled) }
        if (enabled) {
            scheduleAutoExport()
        } else {
            cancelAutoExport()
        }
    }

    private fun onAutoExportIntervalChange(interval: off.kys.backtalk.common.ExportInterval) {
        preferences.autoExportInterval = interval
        _state.update { it.copy(autoExportInterval = interval) }
        if (preferences.autoExportEnabled) {
            scheduleAutoExport()
        }
    }

    private fun onAutoExportFolderChange(uri: Uri) {
        val flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        context.contentResolver.takePersistableUriPermission(uri, flags)
        
        val uriString = uri.toString()
        preferences.autoExportUri = uriString
        _state.update { it.copy(autoExportUri = uriString) }
        
        // If it was just selected, enable it automatically
        if (!preferences.autoExportEnabled) {
            onAutoExportToggle(true)
        } else {
            scheduleAutoExport()
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
        if (enabled) {
            scheduleReminders()
        } else {
            cancelReminders()
            ReminderWorker.cancelSmartReminder(context)
        }
    }

    private fun onReminderIntervalChange(interval: ExportInterval) {
        preferences.reminderInterval = interval
        _state.update { it.copy(reminderInterval = interval) }
        if (preferences.remindersEnabled) {
            scheduleReminders()
        }
    }

    private fun onSmartIntensityChange(intensity: off.kys.backtalk.common.SmartIntensity) {
        preferences.smartReminderIntensity = intensity
        _state.update { it.copy(smartReminderIntensity = intensity) }
        if (preferences.remindersEnabled && preferences.reminderInterval == ExportInterval.SMART) {
            ReminderWorker.scheduleSmartReminder(context)
        }
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

    private fun onRemoveImageMetadataToggle(enabled: Boolean) {
        preferences.removeImageMetadataEnabled = enabled
        _state.update { it.copy(removeImageMetadataEnabled = enabled) }
    }

    private fun onSmartImagePointingToggle(enabled: Boolean) {
        preferences.smartImagePointingEnabled = enabled
        _state.update { it.copy(smartImagePointingEnabled = enabled) }
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

    private fun scheduleAutoExport() {
        val interval = preferences.autoExportInterval
        val workRequest = PeriodicWorkRequestBuilder<AutoExportWorker>(
            interval.hours.toLong(), TimeUnit.HOURS
        )
            .setInitialDelay(interval.hours.toLong(), TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "auto_export_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelAutoExport() {
        WorkManager.getInstance(context).cancelUniqueWork("auto_export_work")
    }

    private fun scheduleReminders() {
        val interval = preferences.reminderInterval
        
        if (interval == ExportInterval.SMART) {
            cancelReminders() // Cancel periodic if it exists
            ReminderWorker.scheduleSmartReminder(context)
            return
        }
        
        // Ensure smart is canceled if we switched to periodic
        ReminderWorker.cancelSmartReminder(context)

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            interval.hours.toLong(), TimeUnit.HOURS
        )
            .setInitialDelay(interval.hours.toLong(), TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminders_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelReminders() {
        WorkManager.getInstance(context).cancelUniqueWork("daily_reminders_work")
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
                            secureScreenEnabled = preferences.secureScreenEnabled,
                            autoUpdateEnabled = preferences.autoUpdateEnabled,
                            isBackupEncrypted = null,
                            selectedBackupUri = null,
                            showOldBackupWarning = result is ImportBackup.ImportResult.SuccessWithWarning
                        )
                    }
                }
                .onFailure { error ->
                    val isWrongPassword =
                        error is BadPaddingException || error.message?.contains(
                            "mac check failed",
                            ignoreCase = true
                        ) == true || error.message?.contains(
                            "pad block corrupted",
                            ignoreCase = true
                        ) == true

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
