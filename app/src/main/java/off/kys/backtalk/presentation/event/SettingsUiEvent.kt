package off.kys.backtalk.presentation.event

import android.net.Uri
import off.kys.backtalk.common.AppDateFormat
import off.kys.backtalk.common.AppTimeFormat
import off.kys.backtalk.common.ThemeMode

/**
 * Represents the various user interactions and internal events occurring on the Settings screen
 */
sealed class SettingsUiEvent {
    data class OnThemeModeChange(val themeMode: ThemeMode) : SettingsUiEvent()
    data class OnLanguageChange(val language: off.kys.backtalk.common.AppLanguage) : SettingsUiEvent()
    data class OnDateFormatChange(val dateFormat: AppDateFormat) : SettingsUiEvent()
    data class OnTimeFormatChange(val timeFormat: AppTimeFormat) : SettingsUiEvent()
    data class OnCustomDateFormatChange(val pattern: String) : SettingsUiEvent()
    data class OnDynamicColorToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnLockToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnLockOnScreenOffToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnLockTimeoutChange(val timeoutMillis: Long) : SettingsUiEvent()
    data class OnSecureScreenToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnAutoUpdateToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnAutoExportToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnAutoExportIntervalChange(val interval: off.kys.backtalk.common.RepeatFrequency) : SettingsUiEvent()
    data class OnAutoExportFolderChange(val uri: Uri) : SettingsUiEvent()
    data class OnAutoExportEncryptionToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnAutoExportPasswordChange(val password: String?) : SettingsUiEvent()
    data class OnRemindersToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnReminderIntervalChange(val interval: off.kys.backtalk.common.RepeatFrequency) : SettingsUiEvent()
    data class OnHapticFeedbackToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnKeepScreenOnToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnDevModeToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnExternalLinkWarningToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnTrimMessagesToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnRemoveImageMetadataToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnSmartImagePointingToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnChangelogVersionUpdate(val version: String) : SettingsUiEvent()

    data class ExportBackup(val uri: Uri, val password: String?) : SettingsUiEvent()
    data class CheckBackupEncryption(val uri: Uri) : SettingsUiEvent()
    data class ImportBackup(val uri: Uri, val password: String?, val clearExisting: Boolean) : SettingsUiEvent()
    
    data object ClearError : SettingsUiEvent()
    data object ClearSuccess : SettingsUiEvent()
    data object ResetBackupState : SettingsUiEvent()
    data object WipeAppData : SettingsUiEvent()
}
