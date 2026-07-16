package off.kys.backtalk.domain.repository

import off.kys.backtalk.domain.model.MediaItem

interface MediaRepository {
    suspend fun fetchGalleryMedia(): List<MediaItem>
}