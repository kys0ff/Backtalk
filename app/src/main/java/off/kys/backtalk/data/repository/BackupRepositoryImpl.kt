package off.kys.backtalk.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import off.kys.backtalk.domain.repository.BackupRepository

/**
 * Implementation of [BackupRepository] using [Context.getContentResolver].
 */
class BackupRepositoryImpl(private val context: Context) : BackupRepository {

    override suspend fun writeBackup(uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
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

    override suspend fun isEncrypted(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val firstChar = inputStream.read()
                if (firstChar == -1) throw IllegalStateException("Empty file")
                firstChar.toChar() != '{'
            } ?: throw IllegalStateException("Could not open input stream")
        }
    }
}
