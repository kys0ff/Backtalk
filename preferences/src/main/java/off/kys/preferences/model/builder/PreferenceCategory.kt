@file:Suppress("FunctionName")

package off.kys.preferences.model.builder

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import off.kys.preferences.core.PreferenceKey
import off.kys.preferences.core.dsl.PreferenceDsl
import off.kys.preferences.model.PreferenceItem

@PreferenceDsl
class PreferenceCategory(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int? = null,
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
        @StringRes titleRes: Int,
        @StringRes summaryRes: Int? = null,
        @DrawableRes icon: Int? = null,
        onClick: (() -> Unit)?
    ) {
        items += PreferenceItem.Action(
            titleRes = titleRes,
            summaryRes = summaryRes,
            iconRes = icon,
            onClick = onClick
        )
    }

    fun Switch(
        key: PreferenceKey.Switch,
        @StringRes titleRes: Int,
        @StringRes summaryRes: Int? = null,
        @DrawableRes iconRes: Int? = null,
        defaultValue: Boolean = false
    ) {
        items += PreferenceItem.Switch(
            key = key,
            titleRes = titleRes,
            iconRes = iconRes,
            summaryRes = summaryRes,
            defaultValue = defaultValue
        )
    }

    fun Slider(
        key: PreferenceKey.Slider,
        @StringRes title: Int,
        @DrawableRes iconRes: Int? = null,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int = 0,
        defaultValue: Float
    ) {
        items += PreferenceItem.Slider(
            key = key,
            titleRes = title,
            iconRes = iconRes,
            valueRange = valueRange,
            steps = steps,
            defaultValue = defaultValue
        )
    }

    fun List(
        key: PreferenceKey.List,
        @StringRes titleRes: Int,
        @DrawableRes iconRes: Int? = null,
        entries: Map<String, String>,
        defaultValue: String
    ) {
        items += PreferenceItem.List(
            key = key,
            titleRes = titleRes,
            iconRes = iconRes,
            entries = entries,
            defaultValue = defaultValue
        )
    }
}