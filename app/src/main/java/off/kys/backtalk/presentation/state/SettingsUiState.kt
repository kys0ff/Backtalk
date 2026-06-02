package off.kys.backtalk.presentation.state

import android.net.Uri
import off.kys.backtalk.common.AppDateFormat
import off.kys.backtalk.common.AppLanguage
import off.kys.backtalk.common.AppTimeFormat
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.SmartIntensity
import off.kys.backtalk.common.ThemeMode

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val dateFormat: AppDateFormat = AppDateFormat.SYSTEM,
    val timeFormat: AppTimeFormat = AppTimeFormat.SYSTEM,
    val customDateFormat: String = "MMM d, yyyy",
    val dynamicColorEnabled: Boolean = false,
    val lockEnabled: Boolean = false,
    val lockOnScreenOff: Boolean = false,
    val lockTimeoutMillis: Long = 0L,
    val secureScreenEnabled: Boolean = false,
    val autoUpdateEnabled: Boolean = false,
    val autoExportEnabled: Boolean = false,
    val autoExportInterval: ExportInterval = ExportInterval.DAILY,
    val autoExportUri: String? = null,
    val autoExportEncrypted: Boolean = false,
    val autoExportPassword: String? = null,
    val remindersEnabled: Boolean = false,
    val reminderInterval: ExportInterval = ExportInterval.DAILY,
    val smartReminderIntensity: SmartIntensity = SmartIntensity.NORMAL,
    val hapticFeedbackEnabled: Boolean = true,
    val keepScreenOn: Boolean = false,
    val devModeEnabled: Boolean = false,
    val externalLinkWarningEnabled: Boolean = true,
    val trimMessagesEnabled: Boolean = false,
    val removeImageMetadataEnabled: Boolean = false,
    val smartImagePointingEnabled: Boolean = false,
    val backupLoading: Boolean = false,
    val isBackupEncrypted: Boolean? = null,
    val wrongPasswordError: Boolean = false,
    val selectedBackupUri: Uri? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val showOldBackupWarning: Boolean = false
) {
    companion object {
        fun empty(): SettingsUiState = SettingsUiState()
    }
}
