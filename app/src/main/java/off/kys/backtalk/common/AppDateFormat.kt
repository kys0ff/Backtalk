package off.kys.backtalk.common

import androidx.annotation.StringRes
import off.kys.backtalk.R

/**
 * Represents the date format options available in the application.
 */
enum class AppDateFormat(@get:StringRes val titleResId: Int, val pattern: String?) {
    SYSTEM(R.string.date_format_system, null),
    DMY_SLASH(R.string.date_format_dmy_slash, "dd/MM/yyyy"),
    MDY_SLASH(R.string.date_format_mdy_slash, "MM/dd/yyyy"),
    YMD_DASH(R.string.date_format_ymd_dash, "yyyy-MM-dd"),
    DMY_DOT(R.string.date_format_dmy_dot, "dd.MM.yyyy"),
    CUSTOM(R.string.date_format_custom, null)
}
