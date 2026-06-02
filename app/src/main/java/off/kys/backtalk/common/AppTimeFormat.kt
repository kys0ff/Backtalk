package off.kys.backtalk.common

import androidx.annotation.StringRes
import off.kys.backtalk.R

/**
 * Represents the time format options available in the application.
 */
enum class AppTimeFormat(@get:StringRes val titleResId: Int) {
    SYSTEM(R.string.time_format_system),
    TWELVE_HOUR(R.string.time_format_12h),
    TWENTY_FOUR_HOUR(R.string.time_format_24h)
}
