package off.kys.backtalk.common

import off.kys.backtalk.R

/**
 * Defines the available time intervals for repeating actions or reminders.
 *
 * @property titleResId The string resource ID used for displaying the frequency in the UI.
 * @property hours The duration of the repeat interval converted into hours.
 */
enum class RepeatFrequency(val titleResId: Int, val hours: Int) {
    EVERY_FOUR_HOURS(R.string.common_every_four_hours, 4),
    EVERY_SIX_HOURS(R.string.common_every_six_hours, 6),
    DAILY(R.string.common_daily, 24),
    TWICE_DAILY(R.string.common_twice_daily, 12),
    WEEKLY(R.string.common_weekly, 168),
    MONTHLY(R.string.common_monthly, 720);
}
