package off.kys.preferences.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import off.kys.preferences.core.PreferenceKey

sealed class PreferenceItem {
    data class Preference(val block: @Composable () -> Unit): PreferenceItem()

    data class Action(
        val title: String,
        val summary: String? = null,
        @DrawableRes val icon: Int? = null,
        val onClick: (() -> Unit)?
    ) : PreferenceItem()

    data class Switch(
        val key: PreferenceKey.Switch,
        val title: String,
        val summary: String? = null,
        val defaultValue: Boolean = false,
        @DrawableRes val icon: Int? = null
    ) : PreferenceItem()

    data class Slider(
        val key: PreferenceKey.Slider,
        val title: String,
        val defaultValue: Float = 0f,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        @DrawableRes val icon: Int? = null
    ) : PreferenceItem()

    // You can add Dialog/List here following the previous pattern
    data class List(
        val key: PreferenceKey.List,
        val title: String,
        val entries: Map<String, String>,
        val defaultValue: String,
        @DrawableRes val icon: Int? = null
    ) : PreferenceItem()
}