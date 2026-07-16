package off.kys.backtalk.domain.model

import android.net.Uri

data class MediaFolder(
    val id: String,
    val name: String,
    val firstItemUri: Uri? = null
)