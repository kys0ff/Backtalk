package off.kys.backtalk.domain.model

import android.net.Uri

/**
 * Represents a backup file with its metadata.
 */
data class BackupFile(
    val uri: Uri,
    val name: String,
    val lastModified: Long
)
