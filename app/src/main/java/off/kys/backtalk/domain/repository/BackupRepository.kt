package off.kys.backtalk.domain.repository

import android.net.Uri

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
     * Reads content from the specified [uri].
     *
     * @param uri The source Uri.
     * @return Result containing the string content or an error.
     */
    suspend fun readBackup(uri: Uri): Result<String>

    /**
     * Checks if the backup at the specified [uri] is encrypted.
     *
     * @param uri The source Uri.
     * @return Result containing true if encrypted, false otherwise.
     */
    suspend fun isEncrypted(uri: Uri): Result<Boolean>
}
