package off.kys.preferences.model

import androidx.compose.ui.graphics.painter.Painter
import off.kys.preferences.model.PreferenceItem

data class PreferenceCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: Painter,
    val items: List<PreferenceItem>, // The list of settings inside this category
)