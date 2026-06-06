package off.kys.backtalk.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import off.kys.backtalk.domain.repository.BackupRepository

/**
 * Implementation of [BackupRepository] using [Context.getContentResolver].
 */
class BackupRepositoryImpl(private val context: Context) : BackupRepository {

    override suspend fun writeBackup(uri: Uri, content: String): Result<Unit> =
        writeBackup(uri, content.toByteArray())

    override suspend fun writeBackup(uri: Uri, bytes: ByteArray): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                } ?: throw IllegalStateException("Could not open output stream")
            }
        }

    override suspend fun readBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IllegalStateException("Could not open input stream")
        }
    }

    override suspend fun readBackupBytes(uri: Uri): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: throw IllegalStateException("Could not open input stream")
        }
    }

    override suspend fun isEncrypted(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(4)
                val read = inputStream.read(buffer)
                if (read < 1) throw IllegalStateException("Empty file")

                val firstChar = buffer[0].toInt().toChar()
                if (firstChar == '{') return@runCatching false // Unencrypted JSON

                if (read >= 2 && buffer[0].toInt() == 0x50 && buffer[1].toInt() == 0x4B) {
                    return@runCatching false // Unencrypted ZIP
                }

                true // Likely encrypted (either JSON or ZIP)
            } ?: throw IllegalStateException("Could not open input stream")
        }
    }

    override suspend fun createBackupFile(directoryUri: Uri, fileName: String): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            DocumentFile.fromTreeUri(context, directoryUri)
                ?.createFile("application/octet-stream", fileName)
                ?.uri ?: throw IllegalStateException("Could not create file")
        }
    }
}
