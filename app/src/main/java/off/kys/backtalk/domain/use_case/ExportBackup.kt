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
    suspend operator fun invoke(uri: Uri, password: String?): Result<Unit> = runCatching {
        val messages = messagesRepository.getAllMessages().first()
        val prefsMap = preferences.getExportablePreferences()

        val backupData = BackupData(messages = messages, preferences = prefsMap)
        val json = Json.encodeToString(backupData)

        val bays = ByteArrayOutputStream()
        ZipOutputStream(bays).use { zos ->
            zos.putNextEntry(ZipEntry("backup.json"))
            zos.write(json.toByteArray())
            zos.closeEntry()

            val allMediaPaths = messages.flatMap { message ->
                listOfNotNull(message.voicePath, message.mediaPath) + (message.mediaPaths ?: emptyList())
            }.filter { it.isNotBlank() }.distinct()

            allMediaPaths.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    try {
                        zos.putNextEntry(ZipEntry("media/${file.name}"))
                        zos.write(file.readBytes())
                        zos.closeEntry()
                    } catch (e: Exception) {
                        e.printStackTrace()
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
