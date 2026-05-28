package off.kys.backtalk.domain.use_case

import android.content.Context
import android.net.Uri
import kotlinx.serialization.json.Json
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.domain.model.BackupData
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.util.CryptoUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

/**
 * Use case to import messages and preferences from a backup file.
 */
class ImportBackup(
    private val context: Context,
    private val messagesDao: MessagesDao,
    private val preferences: BacktalkPreferences,
    private val backupRepository: BackupRepository
) {
    /**
     * Represents the result of an import operation.
     */
    sealed class ImportResult {
        data object Success : ImportResult()
        data object SuccessWithWarning : ImportResult()
    }

    /**
     * Executes the import.
     *
     * @param uri The source Uri.
     * @param password Optional password for decryption.
     * @param clearExisting Whether to delete existing data before importing.
     */
    suspend operator fun invoke(
        uri: Uri,
        password: String?,
        clearExisting: Boolean
    ): Result<ImportResult> =
        runCatching {
            val bytes = backupRepository.readBackupBytes(uri).getOrThrow()
            var isOldFormat = false

            val (json, mediaMap) = if (isZip(bytes) || !isJson(String(bytes))) {
                // Potential ZIP format or Encrypted binary
                if (isZip(bytes)) {
                    // Unencrypted ZIP
                    extractZip(bytes)
                } else {
                    // Could be encrypted ZIP (.bkt) or encrypted legacy JSON (.json)
                    if (password.isNullOrBlank()) {
                        throw IllegalArgumentException("Password required for encrypted backup")
                    }

                    // Try decrypting as binary first (.bkt behavior)
                    val decryptedBytes = try {
                        CryptoUtils.decrypt(bytes, password.toCharArray())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                    if (decryptedBytes != null && isZip(decryptedBytes)) {
                        // It was an encrypted ZIP (.bkt)
                        extractZip(decryptedBytes)
                    } else {
                        // It might be a legacy encrypted JSON (Base64 encoded)
                        val content = String(bytes).trim()
                        val decryptedJson = CryptoUtils.decrypt(content, password.toCharArray())
                        isOldFormat = true
                        decryptedJson to emptyMap()
                    }
                }
            } else {
                // Plain JSON format (Old)
                isOldFormat = true
                String(bytes) to emptyMap()
            }

            val backupData = Json.decodeFromString<BackupData>(json)

            if (clearExisting) {
                messagesDao.deleteAllMessages()
            }

            // Restore Preferences
            backupData.preferences.forEach { (key, value) ->
                when (key) {
                    BacktalkPreferences.KEY_THEME_MODE -> preferences.themeMode =
                        ThemeMode.valueOf(value)
                    BacktalkPreferences.KEY_DYNAMIC_COLOR -> preferences.dynamicColorEnabled =
                        value.toBoolean()
                    BacktalkPreferences.KEY_LOCK_ENABLED -> preferences.lockEnabled =
                        value.toBoolean()
                    BacktalkPreferences.KEY_SECURE_SCREEN -> preferences.secureScreenEnabled =
                        value.toBoolean()
                    BacktalkPreferences.KEY_AUTO_UPDATE -> preferences.autoUpdateEnabled =
                        value.toBoolean()

                }
            }

            // Restore Media and Messages
            val mediaDir = File(context.filesDir, "media").apply { mkdirs() }
            backupData.messages.forEach { message ->
                var updatedMessage = message
                
                message.voicePath?.let { oldPath ->
                    val fileName = File(oldPath).name
                    val mediaBytes = mediaMap[fileName]
                    if (mediaBytes != null) {
                        val newFile = File(mediaDir, fileName)
                        newFile.writeBytes(mediaBytes)
                        updatedMessage = updatedMessage.copy(voicePath = newFile.absolutePath)
                    }
                }

                message.mediaPath?.let { oldPath ->
                    val fileName = File(oldPath).name
                    val mediaBytes = mediaMap[fileName]
                    if (mediaBytes != null) {
                        val newFile = File(mediaDir, fileName)
                        newFile.writeBytes(mediaBytes)
                        updatedMessage = updatedMessage.copy(mediaPath = newFile.absolutePath)
                    }
                }

                message.mediaPaths?.let { oldPaths ->
                    val newPaths = oldPaths.map { oldPath ->
                        val fileName = File(oldPath).name
                        val mediaBytes = mediaMap[fileName]
                        if (mediaBytes != null) {
                            val newFile = File(mediaDir, fileName)
                            newFile.writeBytes(mediaBytes)
                            newFile.absolutePath
                        } else {
                            oldPath
                        }
                    }
                    updatedMessage = updatedMessage.copy(mediaPaths = newPaths)
                }

                messagesDao.insertMessage(updatedMessage)
            }

            if (isOldFormat) ImportResult.SuccessWithWarning else ImportResult.Success
        }

    private fun isZip(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0].toInt() == 0x50 && bytes[1].toInt() == 0x4B

    private fun isJson(content: String): Boolean =
        content.trim().startsWith("{") && content.trim().endsWith("}")

    private fun extractZip(zipBytes: ByteArray): Pair<String, Map<String, ByteArray>> {
        var json = ""
        val mediaMap = mutableMapOf<String, ByteArray>()

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name == "backup.json" -> {
                        json = zis.readBytes().decodeToString()
                    }
                    entry.name.startsWith("media/") -> {
                        val fileName = entry.name.removePrefix("media/")
                        if (fileName.isNotEmpty()) {
                            mediaMap[fileName] = zis.readBytes()
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (json.isEmpty()) throw IllegalArgumentException("Invalid backup: backup.json missing")
        return json to mediaMap
    }
}
