package off.kys.backtalk.domain.model

import android.net.Uri

data class MediaItem(
    val uri: Uri,
    val type: String,
    val bucketId: String? = null,
    val bucketName: String? = null
)