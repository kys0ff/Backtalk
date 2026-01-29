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

    fun Section(@StringRes titleRes: Int) {
        items += PreferenceItem.Section(titleRes)
    }

    fun Preference(block: @Composable () -> Unit) {
        items += PreferenceItem.Preference(block)
    }

    fun Action(
        @StringRes titleRes: Int,
        @StringRes summaryRes: Int? = null,
        @DrawableRes icon: Int? = null,
        enabled: Boolean = true,
        onClick: (() -> Unit)?
    ) {
        items += PreferenceItem.Action(
            titleRes = titleRes,
            summaryRes = summaryRes,
            iconRes = icon,
            enabled = enabled,
            onClick = onClick
        )
    }

    fun Switch(
        key: PreferenceKey.Switch,
        @StringRes titleRes: Int,
        @StringRes summaryRes: Int? = null,
        @DrawableRes iconRes: Int? = null,
        enabled: Boolean = true,
        defaultValue: Boolean = false
    ) {
        items += PreferenceItem.Switch(
            key = key,
            titleRes = titleRes,
            iconRes = iconRes,
            summaryRes = summaryRes,
            enabled = enabled,
            defaultValue = defaultValue
        )
    }

    fun Slider(
        key: PreferenceKey.Slider,
        @StringRes title: Int,
        @DrawableRes iconRes: Int? = null,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int = 0,
        defaultValue: Float,
        enabled: Boolean = true,
    ) {
        items += PreferenceItem.Slider(
            key = key,
            titleRes = title,
            iconRes = iconRes,
            valueRange = valueRange,
            steps = steps,
            defaultValue = defaultValue,
            enabled = enabled
        )
    }

    fun List(
        key: PreferenceKey.List,
        @StringRes titleRes: Int,
        @DrawableRes iconRes: Int? = null,
        entries: Map<String, String>,
        defaultValue: String,
        enabled: Boolean = true,
    ) {
        items += PreferenceItem.List(
            key = key,
            titleRes = titleRes,
            iconRes = iconRes,
            entries = entries,
            defaultValue = defaultValue,
            enabled = enabled
        )
    }
}