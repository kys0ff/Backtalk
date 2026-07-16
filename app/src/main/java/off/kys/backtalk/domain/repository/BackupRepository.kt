package off.kys.backtalk.domain.repository

import android.net.Uri
import off.kys.backtalk.domain.model.BackupFile

/**
 * Interface for repository handling backup file operations.
 */
interface BackupRepository {

    /**
     * Writes [content] to the specified [uri].
     *
     * @param uri The destination Uri.
     * @param content The string content to write.
     * @return Result indicating success or failure.
     */
    suspend fun writeBackup(uri: Uri, content: String): Result<Unit>

    /**
     * Writes [bytes] to the specified [uri].
     *
     * @param uri The destination Uri.
     * @param bytes The byte array content to write.
     * @return Result indicating success or failure.
     */
    suspend fun writeBackup(uri: Uri, bytes: ByteArray): Result<Unit>

    /**
     * Reads content from the specified [uri].
     *
     * @param uri The source Uri.
     * @return Result containing the string content or an error.
     */
    suspend fun readBackup(uri: Uri): Result<String>

    /**
     * Reads byte content from the specified [uri].
     *
     * @param uri The source Uri.
     * @return Result containing the byte array or an error.
     */
    suspend fun readBackupBytes(uri: Uri): Result<ByteArray>

    /**
     * Checks if the backup at the specified [uri] is encrypted.
     *
     * @param uri The source Uri.
     * @return Result containing true if encrypted, false otherwise.
     */
    suspend fun isEncrypted(uri: Uri): Result<Boolean>

    /**
     * Creates a new backup file in the specified [directoryUri].
     *
     * @param directoryUri The parent directory Uri.
     * @param fileName The name of the file to create.
     * @return Result containing the Uri of the created file.
     */
    suspend fun createBackupFile(directoryUri: Uri, fileName: String): Result<Uri>

    /**
     * Retrieves all backup files in the specified [directoryUri].
     *
     * @param directoryUri The parent directory Uri.
     * @return Result containing a list of [BackupFile]s.
     */
    suspend fun getBackupFiles(directoryUri: Uri): Result<List<BackupFile>>

    /**
     * Deletes the backup file at the specified [uri].
     *
     * @param uri The Uri of the file to delete.
     * @return Result indicating success or failure.
     */
    suspend fun deleteBackup(uri: Uri): Result<Unit>
}
