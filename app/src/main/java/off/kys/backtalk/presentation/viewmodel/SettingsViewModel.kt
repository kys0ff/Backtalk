package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.R
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

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnThemeModeChange -> {
                preferences.themeMode = event.themeMode
                _state.update { it.copy(themeMode = event.themeMode) }
            }

            is SettingsUiEvent.OnDynamicColorToggle -> {
                preferences.dynamicColorEnabled = event.enabled
                _state.update { it.copy(dynamicColorEnabled = event.enabled) }
            }

            is SettingsUiEvent.OnLockToggle -> {
                preferences.lockEnabled = event.enabled
                _state.update { it.copy(lockEnabled = event.enabled) }
            }

            is SettingsUiEvent.OnSecureScreenToggle -> {
                preferences.secureScreenEnabled = event.enabled
                _state.update { it.copy(secureScreenEnabled = event.enabled) }
            }

            is SettingsUiEvent.OnAutoUpdateToggle -> {
                preferences.autoUpdateEnabled = event.enabled
                _state.update { it.copy(autoUpdateEnabled = event.enabled) }
            }

            is SettingsUiEvent.ExportBackup -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            backupLoading = true,
                            error = null,
                            successMessage = null
                        )
                    }
                    backupUseCases.exportBackup(event.uri, event.password)
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

            is SettingsUiEvent.CheckBackupEncryption -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            backupLoading = true,
                            error = null,
                            isBackupEncrypted = null,
                            selectedBackupUri = event.uri
                        )
                    }
                    backupUseCases.isEncrypted(event.uri)
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

            is SettingsUiEvent.ImportBackup -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            backupLoading = true,
                            error = null,
                            successMessage = null,
                            wrongPasswordError = false
                        )
                    }
                    backupUseCases.importBackup(event.uri, event.password, event.clearExisting)
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

            SettingsUiEvent.ClearError -> _state.update { it.copy(error = null) }
            SettingsUiEvent.ClearSuccess -> _state.update { it.copy(successMessage = null) }
            SettingsUiEvent.ResetBackupState -> _state.update {
                it.copy(
                    isBackupEncrypted = null,
                    selectedBackupUri = null,
                    wrongPasswordError = false
                )
            }
        }
    }
}
