package off.kys.backtalk.domain.use_case

import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.domain.model.BackupData
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.util.CryptoUtils

/**
 * Use case to export messages and preferences to a backup file.
 */
class ExportBackup(
    private val messagesRepository: MessagesRepository,
    private val preferences: BacktalkPreferences,
    private val backupRepository: BackupRepository
) {
    /**
     * Executes the export.
     *
     * @param uri The destination Uri.
     * @param password Optional password for encryption.
     */
    suspend operator fun invoke(uri: Uri, password: String?): Result<Unit> {
        return runCatching {
            val messages = messagesRepository.getAllMessages().first()
            val prefsMap = mapOf(
                BacktalkPreferences.KEY_THEME_MODE to preferences.themeMode.name,
                BacktalkPreferences.KEY_DYNAMIC_COLOR to preferences.dynamicColorEnabled.toString(),
                BacktalkPreferences.KEY_LOCK_ENABLED to preferences.lockEnabled.toString(),
                BacktalkPreferences.KEY_SECURE_SCREEN to preferences.secureScreenEnabled.toString(),
                BacktalkPreferences.KEY_AUTO_UPDATE to preferences.autoUpdateEnabled.toString()
            )

            val backupData = BackupData(messages = messages, preferences = prefsMap)
            val json = Json.encodeToString(backupData)

            val finalContent = if (!password.isNullOrBlank()) {
                CryptoUtils.encrypt(json, password.toCharArray())
            } else {
                json
            }

            backupRepository.writeBackup(uri, finalContent).getOrThrow()
        }
    }
}
