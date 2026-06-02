package off.kys.backtalk.common

import androidx.annotation.StringRes
import off.kys.backtalk.R

/**
 * Represents the theme mode of the application.
 */
enum class ThemeMode(@StringRes val titleResId: Int) {
    /**
     * Forced light theme.
     */
    LIGHT(R.string.theme_light),

    /**
     * Forced dark theme.
     */
    DARK(R.string.theme_dark),

    /**
     * Theme follows the system setting.
     */
    AUTO(R.string.theme_system);

    /**
     * Determines if dark theme should be applied based on the [ThemeMode]
     * and the current system setting.
     */
    fun isDark(systemInDark: Boolean): Boolean = when (this) {
        DARK -> true
        LIGHT -> false
        AUTO -> systemInDark
    }
}
