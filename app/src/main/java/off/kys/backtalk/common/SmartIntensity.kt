package off.kys.backtalk.common

import off.kys.backtalk.R

/**
 * Represents the intensity levels for smart reminders.
 * Each level has a multiplier that scales the average usage interval.
 */
enum class SmartIntensity(val titleResId: Int, val multiplier: Float) {
    RELAXED(R.string.smart_intensity_relaxed, 1.5f),
    NORMAL(R.string.smart_intensity_normal, 1.0f),
    FREQUENT(R.string.smart_intensity_frequent, 0.5f),
    INTENSE(R.string.smart_intensity_intense, 0.25f)
}
