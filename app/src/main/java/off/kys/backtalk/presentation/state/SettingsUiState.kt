package off.kys.backtalk.presentation.state

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
    val backupLoading: Boolean = false,
    val isBackupEncrypted: Boolean? = null,
    val wrongPasswordError: Boolean = false,
    val selectedBackupUri: android.net.Uri? = null,
    val error: String? = null,
    val successMessage: String? = null
)
