@file:Suppress("FunctionName")

package off.kys.preferences.core

import androidx.annotation.DrawableRes
import off.kys.preferences.core.dsl.PreferenceDsl
import off.kys.preferences.model.builder.PreferenceCategory as PreferenceCategoryBuilder

@PreferenceDsl
class PreferenceScreenScope {
    internal val categories = mutableListOf<PreferenceCategoryBuilder>()

    fun PreferenceCategory(
        title: String,
        description: String? = null,
        @DrawableRes icon: Int? = null,
        build: PreferenceCategoryBuilder.() -> Unit
    ) {
        categories += PreferenceCategoryBuilder(
            title = title,
            description = description,
            icon = icon,
            build = build
        )
    }
}