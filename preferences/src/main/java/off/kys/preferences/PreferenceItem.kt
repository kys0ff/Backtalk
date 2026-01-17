package off.kys.preferences

import androidx.compose.ui.graphics.painter.Painter

sealed class PreferenceItem {
    data class Action(
        val title: String,
        val summary: String? = null,
        val icon: Painter? = null,
        val onClick: (() -> Unit)?
    ) : PreferenceItem()

    data class Switch(
        val key: PreferenceKey.Switch,
        val title: String,
        val summary: String? = null,
        val defaultValue: Boolean = false,
        val icon: Painter? = null
    ) : PreferenceItem()

    data class Slider(
        val key: PreferenceKey.Slider,
        val title: String,
        val defaultValue: Float = 0f,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        val icon: Painter? = null
    ) : PreferenceItem()

    // You can add Dialog/List here following the previous pattern
    data class List(
        val key: PreferenceKey.List,
        val title: String,
        val options: Map<String, String>,
        val defaultValue: String,
        val icon: Painter? = null
    ) : PreferenceItem()
}