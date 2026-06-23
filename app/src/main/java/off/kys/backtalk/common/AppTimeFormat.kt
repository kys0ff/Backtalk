package off.kys.backtalk.common

import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.StringRes
import off.kys.backtalk.R

/**
 * Represents the time format options available in the application.
 */
enum class AppTimeFormat(@get:StringRes val titleResId: Int) {
    SYSTEM(R.string.time_format_system),
    TWELVE_HOUR(R.string.time_format_12h),
    TWENTY_FOUR_HOUR(R.string.time_format_24h);

    /**
     * Determines if this format resolves to a 24-hour clock.
     *
     * @param context Required to check the system-wide preference if [SYSTEM] is selected.
     */
    fun is24Hour(context: Context): Boolean = when (this) {
        SYSTEM -> DateFormat.is24HourFormat(context)
        TWELVE_HOUR -> false
        TWENTY_FOUR_HOUR -> true
    }
}