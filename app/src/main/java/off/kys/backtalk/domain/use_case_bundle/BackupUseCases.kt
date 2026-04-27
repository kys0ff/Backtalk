package off.kys.backtalk.domain.use_case_bundle

import android.net.Uri
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.domain.use_case.ExportBackup
import off.kys.backtalk.domain.use_case.ImportBackup

/**
 * Bundle for backup-related use cases.
 */
data class BackupUseCases(
    private val backupRepository: BackupRepository,
    val exportBackup: ExportBackup,
    val importBackup: ImportBackup
) {
    suspend fun isEncrypted(uri: Uri): Result<Boolean> = backupRepository.isEncrypted(uri)
}
