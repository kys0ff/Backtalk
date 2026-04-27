package off.kys.backtalk.presentation.event

import android.net.Uri
import off.kys.backtalk.common.ThemeMode

/**
 * Represents the various user interactions and internal events occurring on the Settings screen
 */
sealed class SettingsUiEvent {
    data class OnThemeModeChange(val themeMode: ThemeMode) : SettingsUiEvent()
    data class OnDynamicColorToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnLockToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnSecureScreenToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnAutoUpdateToggle(val enabled: Boolean) : SettingsUiEvent()
    
    data class ExportBackup(val uri: Uri, val password: String?) : SettingsUiEvent()
    data class CheckBackupEncryption(val uri: Uri) : SettingsUiEvent()
    data class ImportBackup(val uri: Uri, val password: String?, val clearExisting: Boolean) : SettingsUiEvent()
    
    data object ClearError : SettingsUiEvent()
    data object ClearSuccess : SettingsUiEvent()
    data object ResetBackupState : SettingsUiEvent()
}
