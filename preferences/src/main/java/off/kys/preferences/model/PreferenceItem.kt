package off.kys.preferences.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import off.kys.preferences.core.PreferenceKey

sealed class PreferenceItem {
    data class Preference(val block: @Composable () -> Unit) : PreferenceItem()

    data class Action(
        @StringRes val titleRes: Int,
        @StringRes val summaryRes: Int? = null,
        @DrawableRes val iconRes: Int? = null,
        val onClick: (() -> Unit)?
    ) : PreferenceItem()

    data class Switch(
        val key: PreferenceKey.Switch,
        @StringRes val titleRes: Int,
        @StringRes val summaryRes: Int? = null,
        val defaultValue: Boolean = false,
        @DrawableRes val iconRes: Int? = null
    ) : PreferenceItem()

    data class Slider(
        val key: PreferenceKey.Slider,
        @StringRes val titleRes: Int,
        val defaultValue: Float = 0f,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        @DrawableRes val iconRes: Int? = null
    ) : PreferenceItem()

    // You can add Dialog/List here following the previous pattern
    data class List(
        val key: PreferenceKey.List,
        @StringRes val titleRes: Int,
        val entries: Map<String, String>,
        val defaultValue: String,
        @DrawableRes val iconRes: Int? = null
    ) : PreferenceItem()
}