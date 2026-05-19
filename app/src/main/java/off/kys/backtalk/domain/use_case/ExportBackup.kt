package off.kys.backtalk.domain.use_case

import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.domain.model.BackupData
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.util.CryptoUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

            val bays = ByteArrayOutputStream()
            ZipOutputStream(bays).use { zos ->
                zos.putNextEntry(ZipEntry("backup.json"))
                zos.write(json.toByteArray())
                zos.closeEntry()

                messages.forEach { message ->
                    val paths = listOfNotNull(message.voicePath, message.mediaPath)
                    paths.forEach { path ->
                        val file = File(path)
                        if (file.exists()) {
                            zos.putNextEntry(ZipEntry("media/${file.name}"))
                            zos.write(file.readBytes())
                            zos.closeEntry()
                        }
                    }
                }
            }

            val zipBytes = bays.toByteArray()

            val finalContent = if (!password.isNullOrBlank()) {
                CryptoUtils.encrypt(zipBytes, password.toCharArray())
            } else {
                zipBytes
            }

            backupRepository.writeBackup(uri, finalContent).getOrThrow()
        }
    }
}
