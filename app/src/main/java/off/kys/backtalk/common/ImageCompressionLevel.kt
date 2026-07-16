package off.kys.backtalk.common

import androidx.annotation.StringRes
import off.kys.backtalk.R

/**
 * Represents the compression level for images sent in the application.
 */
enum class ImageCompressionLevel(
    @get:StringRes val titleResId: Int,
    val quality: Int
) {
    /**
     * No compression applied.
     */
    ORIGINAL(R.string.image_compression_original, 100),

    /**
     * High quality compression.
     */
    BEST(R.string.image_compression_best, 95),

    /**
     * High quality compression.
     */
    HIGH(R.string.image_compression_high, 85),

    /**
     * Balanced compression.
     */
    BALANCED(R.string.image_compression_balanced, 70),

    /**
     * Low quality compression to save data.
     */
    LOW(R.string.image_compression_low, 50),

    /**
     * Very low quality compression for maximum data saving.
     */
    VERY_LOW(R.string.image_compression_very_low, 30);
}
