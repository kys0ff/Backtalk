package off.kys.backtalk.data.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.domain.use_case.ExportBackup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker that performs automated backups in the background.
 */
class AutoExportWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val preferences: BacktalkPreferences by inject()
    private val exportBackup: ExportBackup by inject()
    private val backupRepository: BackupRepository by inject()

    override suspend fun doWork(): Result {
        if (!preferences.autoExportEnabled) return Result.success()

        val directoryUriString = preferences.autoExportUri ?: return Result.failure()
        val directoryUri = directoryUriString.toUri()
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "backtalk_backup_$timestamp.json"

        return try {
            val fileUri = backupRepository.createBackupFile(directoryUri, fileName).getOrThrow()
            val password = if (preferences.autoExportEncrypted) preferences.autoExportPassword else null
            
            exportBackup(fileUri, password).fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
