package off.kys.preferences

import androidx.compose.ui.graphics.painter.Painter

data class PreferenceCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: Painter,
    val items: List<PreferenceItem>, // The list of settings inside this category
)