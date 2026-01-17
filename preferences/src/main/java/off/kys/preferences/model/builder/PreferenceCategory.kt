@file:Suppress("FunctionName")

package off.kys.preferences.model.builder

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import off.kys.preferences.core.PreferenceKey
import off.kys.preferences.core.dsl.PreferenceDsl
import off.kys.preferences.model.PreferenceItem

@PreferenceDsl
class PreferenceCategory(
    val title: String,
    val description: String? = null,
    @DrawableRes val icon: Int? = null,
    val build: PreferenceCategory.() -> Unit
) {
    internal val items = mutableListOf<PreferenceItem>()

    init {
        build()
    }

    fun Preference(block: @Composable () -> Unit) {
        items += PreferenceItem.Preference(block)
    }

    fun Action(
        title: String,
        summary: String? = null,
        @DrawableRes icon: Int? = null,
        onClick: (() -> Unit)?
    ) {
        items += PreferenceItem.Action(
            title = title,
            summary = summary,
            icon = icon,
            onClick = onClick
        )
    }

    fun Switch(
        key: PreferenceKey.Switch,
        title: String,
        @DrawableRes icon: Int? = null,
        summary: String? = null,
        defaultValue: Boolean = false
    ) {
        items += PreferenceItem.Switch(
            key = key,
            title = title,
            icon = icon,
            summary = summary,
            defaultValue = defaultValue
        )
    }

    fun Slider(
        key: PreferenceKey.Slider,
        title: String,
        @DrawableRes icon: Int? = null,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int = 0,
        defaultValue: Float
    ) {
        items += PreferenceItem.Slider(
            key = key,
            title = title,
            icon = icon,
            valueRange = valueRange,
            steps = steps,
            defaultValue = defaultValue
        )
    }

    fun List(
        key: PreferenceKey.List,
        title: String,
        @DrawableRes icon: Int? = null,
        entries: Map<String, String>,
        defaultValue: String
    ) {
        items += PreferenceItem.List(
            key = key,
            title = title,
            icon = icon,
            entries = entries,
            defaultValue = defaultValue
        )
    }
}