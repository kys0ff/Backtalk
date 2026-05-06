package off.kys.backtalk.presentation.state

import android.net.Uri
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.ThemeMode

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val dynamicColorEnabled: Boolean = false,
    val lockEnabled: Boolean = false,
    val secureScreenEnabled: Boolean = false,
    val autoUpdateEnabled: Boolean = false,
    val autoExportEnabled: Boolean = false,
    val autoExportInterval: ExportInterval = ExportInterval.DAILY,
    val autoExportUri: String? = null,
    val autoExportEncrypted: Boolean = false,
    val autoExportPassword: String? = null,
    val hapticFeedbackEnabled: Boolean = true,
    val backupLoading: Boolean = false,
    val isBackupEncrypted: Boolean? = null,
    val wrongPasswordError: Boolean = false,
    val selectedBackupUri: Uri? = null,
    val error: String? = null,
    val successMessage: String? = null
)
