package off.kys.preferences.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import off.kys.preferences.core.PreferenceKey

sealed class PreferenceItem {
    data class Preference(val block: @Composable () -> Unit) : PreferenceItem()

    data class Section(@StringRes val titleRes: Int) : PreferenceItem()

    data class Info(
        @StringRes val infoRes: Int
    ) : PreferenceItem()

    data class Action(
        @StringRes val titleRes: Int,
        @StringRes val summaryRes: Int? = null,
        @DrawableRes val iconRes: Int? = null,
        val enabled: Boolean = true,
        val onClick: (() -> Unit)?
    ) : PreferenceItem()

    data class Switch(
        val key: PreferenceKey.Switch,
        @StringRes val titleRes: Int,
        @StringRes val summaryRes: Int? = null,
        @DrawableRes val iconRes: Int? = null,
        val defaultValue: Boolean = false,
        val enabled: Boolean = true
    ) : PreferenceItem()

    data class Slider(
        val key: PreferenceKey.Slider,
        @StringRes val titleRes: Int,
        @DrawableRes val iconRes: Int? = null,
        val defaultValue: Float = 0f,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        val enabled: Boolean = true
    ) : PreferenceItem()

    // You can add Dialog/List here following the previous pattern
    data class List(
        val key: PreferenceKey.List,
        @StringRes val titleRes: Int,
        @DrawableRes val iconRes: Int? = null,
        val entries: Map<String, String>,
        val defaultValue: String,
        val enabled: Boolean = true
    ) : PreferenceItem()
}