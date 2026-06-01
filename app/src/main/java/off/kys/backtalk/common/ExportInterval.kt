package off.kys.backtalk.common

import off.kys.backtalk.R

/**
 * Represents the intervals at which auto-exports or reminders can occur.
 */
enum class ExportInterval(val titleResId: Int, val hours: Int) {
    DAILY(R.string.common_daily, 24),
    TWICE_DAILY(R.string.common_twice_daily, 12),
    EVERY_SIX_HOURS(R.string.common_every_six_hours, 6),
    EVERY_FOUR_HOURS(R.string.common_every_four_hours, 4),
    WEEKLY(R.string.common_weekly, 168),
    MONTHLY(R.string.common_monthly, 720),
    SMART(R.string.common_smart, 0);

    val days: Int get() = hours / 24
}
