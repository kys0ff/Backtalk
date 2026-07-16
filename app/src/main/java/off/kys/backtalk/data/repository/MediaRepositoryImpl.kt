package off.kys.backtalk.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import off.kys.backtalk.domain.model.MediaItem
import off.kys.backtalk.domain.repository.MediaRepository

class MediaRepositoryImpl(private val context: Context) : MediaRepository {

    override suspend fun fetchGalleryMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        val queryUri = MediaStore.Files.getContentUri("external")
        val selection =
            "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}) OR (${MediaStore.MediaColumns.MIME_TYPE} = ?)"
        val selectionArgs = arrayOf("image/svg+xml")

        context.contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)
            ?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
                val bucketNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val mimeType = cursor.getString(mimeColumn)
                    val bucketId = cursor.getString(bucketIdColumn)
                    val bucketName = cursor.getString(bucketNameColumn)
                    val uri = ContentUris.withAppendedId(queryUri, id)
                    items.add(MediaItem(uri, mimeType, bucketId, bucketName))
                }
            }
        items
    }
}