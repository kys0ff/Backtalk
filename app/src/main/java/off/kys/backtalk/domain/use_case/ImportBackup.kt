package off.kys.backtalk.domain.use_case

import android.net.Uri
import kotlinx.serialization.json.Json
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.domain.model.BackupData
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.util.CryptoUtils

/**
 * Use case to import messages and preferences from a backup file.
 */
class ImportBackup(
    private val messagesDao: MessagesDao,
    private val preferences: BacktalkPreferences,
    private val backupRepository: BackupRepository
) {
    /**
     * Executes the import.
     *
     * @param uri The source Uri.
     * @param password Optional password for decryption.
     * @param clearExisting Whether to delete existing data before importing.
     */
    suspend operator fun invoke(uri: Uri, password: String?, clearExisting: Boolean): Result<Unit> =
        runCatching {
            val content = backupRepository.readBackup(uri).getOrThrow()

            val json = if (isJson(content)) {
                content
            } else {
                if (password.isNullOrBlank()) {
                    throw IllegalArgumentException("Password required for encrypted backup")
                }
                CryptoUtils.decrypt(content, password.toCharArray())
            }

            val backupData = Json.decodeFromString<BackupData>(json)

            if (clearExisting) {
                messagesDao.deleteAllMessages()
            }

            // Restore Preferences
            backupData.preferences.forEach { (key, value) ->
                when (key) {
                    BacktalkPreferences.KEY_THEME_MODE -> preferences.themeMode = ThemeMode.valueOf(value)
                    BacktalkPreferences.KEY_DYNAMIC_COLOR -> preferences.dynamicColorEnabled = value.toBoolean()
                    BacktalkPreferences.KEY_LOCK_ENABLED -> preferences.lockEnabled = value.toBoolean()
                    BacktalkPreferences.KEY_SECURE_SCREEN -> preferences.secureScreenEnabled = value.toBoolean()
                    BacktalkPreferences.KEY_AUTO_UPDATE -> preferences.autoUpdateEnabled = value.toBoolean()
                }
            }

            // Restore Messages
            backupData.messages.forEach {
                messagesDao.insertMessage(it)
            }
        }

    private fun isJson(content: String): Boolean = content.trim().startsWith("{") && content.trim().endsWith("}")
}
