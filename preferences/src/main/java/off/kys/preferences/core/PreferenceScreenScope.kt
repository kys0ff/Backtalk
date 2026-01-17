@file:Suppress("FunctionName")

package off.kys.preferences.core

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import off.kys.preferences.core.dsl.PreferenceDsl
import off.kys.preferences.model.builder.PreferenceCategory as PreferenceCategoryBuilder

@PreferenceDsl
class PreferenceScreenScope {
    internal val categories = mutableListOf<PreferenceCategoryBuilder>()

    fun PreferenceCategory(
        @StringRes titleRes: Int,
        @StringRes descriptionRes: Int? = null,
        @DrawableRes iconRes: Int? = null,
        build: PreferenceCategoryBuilder.() -> Unit
    ) {
        categories += PreferenceCategoryBuilder(
            titleRes = titleRes,
            descriptionRes = descriptionRes,
            icon = iconRes,
            build = build
        )
    }
}