package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.domain.use_case_bundle.BackupUseCases
import off.kys.backtalk.presentation.event.SettingsUiEvent
import off.kys.backtalk.presentation.state.SettingsUiState
import javax.crypto.BadPaddingException

/**
 * ViewModel for the Settings screen.
 */
class SettingsViewModel(
    private val application: Application,
    private val preferences: BacktalkPreferences,
    private val backupUseCases: BackupUseCases
) : AndroidViewModel(application) {

    private val context: Context by lazy { application.applicationContext }

    private val _state = MutableStateFlow(
        SettingsUiState(
            themeMode = preferences.themeMode,
            dynamicColorEnabled = preferences.dynamicColorEnabled,
            lockEnabled = preferences.lockEnabled,
            secureScreenEnabled = preferences.secureScreenEnabled,
            autoUpdateEnabled = preferences.autoUpdateEnabled
        )
    )
    val state = _state.asStateFlow()

    fun onEvent(event: SettingsUiEvent) = when (event) {
        is SettingsUiEvent.OnThemeModeChange -> onThemeModeChange(event.themeMode)
        is SettingsUiEvent.OnDynamicColorToggle -> onDynamicColorToggle(event.enabled)
        is SettingsUiEvent.OnLockToggle -> onLockToggle(event.enabled)
        is SettingsUiEvent.OnSecureScreenToggle -> onSecureScreenToggle(event.enabled)
        is SettingsUiEvent.OnAutoUpdateToggle -> onAutoUpdateToggle(event.enabled)
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
    }

    private fun onThemeModeChange(themeMode: ThemeMode) {
        preferences.themeMode = themeMode
        _state.update { it.copy(themeMode = themeMode) }
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
                            successMessage = context.getString(R.string.backup_exported_successfully)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            backupLoading = false,
                            error = error.message
                                ?: context.getString(R.string.export_failed)
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
                                ?: context.getString(R.string.failed_to_check_backup)
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
                .onSuccess {
                    _state.update {
                        it.copy(
                            backupLoading = false,
                            successMessage = context.getString(R.string.backup_imported_successfully),
                            themeMode = preferences.themeMode,
                            dynamicColorEnabled = preferences.dynamicColorEnabled,
                            lockEnabled = preferences.lockEnabled,
                            secureScreenEnabled = preferences.secureScreenEnabled,
                            autoUpdateEnabled = preferences.autoUpdateEnabled,
                            isBackupEncrypted = null,
                            selectedBackupUri = null
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
                                ?: context.getString(R.string.import_failed)),
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
                wrongPasswordError = false
            )
        }
    }
}
