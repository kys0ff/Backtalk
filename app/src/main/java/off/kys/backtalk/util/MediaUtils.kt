package off.kys.backtalk.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

/**
 * Utility for media processing.
 */
object MediaUtils {
    /**
     * Strips common sensitive EXIF metadata from an image file.
     */
    fun stripImageMetadata(file: File) {
        runCatching {
            val exifInterface = ExifInterface(file.absolutePath)
            val tagsToRemove = arrayOf(
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_GPS_PROCESSING_METHOD,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_DATETIME_ORIGINAL,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_SOFTWARE,
                ExifInterface.TAG_USER_COMMENT,
                ExifInterface.TAG_ARTIST,
                ExifInterface.TAG_COPYRIGHT,
                ExifInterface.TAG_CAMERA_OWNER_NAME,
                ExifInterface.TAG_BODY_SERIAL_NUMBER,
                ExifInterface.TAG_LENS_MAKE,
                ExifInterface.TAG_LENS_MODEL,
                ExifInterface.TAG_LENS_SERIAL_NUMBER
            )
            
            var changed = false
            tagsToRemove.forEach { tag ->
                if (exifInterface.getAttribute(tag) != null) {
                    exifInterface.setAttribute(tag, null)
                    changed = true
                }
            }
            
            if (changed) {
                exifInterface.saveAttributes()
            }
        }
    }

    /**
     * Compresses an image file to a specified quality.
     */
    fun compressImage(file: File, quality: Int) {
        if (quality >= 100) return
        
        runCatching {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            bitmap.recycle()
        }
    }
}
