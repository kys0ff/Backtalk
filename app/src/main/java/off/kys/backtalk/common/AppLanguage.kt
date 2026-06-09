package off.kys.backtalk.common

import androidx.annotation.StringRes
import off.kys.backtalk.R
import off.kys.backtalk.util.emptyString

enum class AppLanguage(
    val tag: String,
    @get:StringRes val displayNameRes: Int
) {
    SYSTEM(emptyString(), R.string.language_system),
    ENGLISH("en", R.string.language_english),
    ARABIC("ar", R.string.language_arabic),
    SPANISH("es", R.string.language_spanish);

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return SYSTEM
            val primaryTag = tag.split(",")[0].split("-")[0]
            return entries.find { it.tag == primaryTag } ?: SYSTEM
        }
    }
}
