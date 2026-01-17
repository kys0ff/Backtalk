package off.kys.preferences.compose.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import off.kys.preferences.R
import off.kys.preferences.compose.ui.item.ActionPreferenceItem
import off.kys.preferences.compose.ui.item.ListPreference
import off.kys.preferences.compose.ui.item.PreferenceCategoryItem
import off.kys.preferences.compose.ui.item.SliderPreferenceItem
import off.kys.preferences.compose.ui.item.SwitchPreference
import off.kys.preferences.data.PreferenceManager
import off.kys.preferences.model.PreferenceCategory
import off.kys.preferences.model.PreferenceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreen(
    categories: List<PreferenceCategory>
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }

    // STATE: Keeps track of which category is currently open (null = Main Menu)
    var selectedCategory by remember { mutableStateOf<PreferenceCategory?>(null) }

    // Handle System Back Button
    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedCategory?.title ?: "Settings") },
                navigationIcon = {
                    // Show Back Arrow only if we are inside a category
                    if (selectedCategory != null) {
                        IconButton(onClick = { selectedCategory = null }) {
                            Icon(
                                painter = painterResource(R.drawable.round_arrow_back_24),
                                contentDescription = "Navigate up"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        // Simple Animation to slide between screens
        AnimatedContent(
            targetState = selectedCategory,
            modifier = Modifier.padding(padding),
            label = "SettingsTransition",
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            }
        ) { category ->
            if (category == null) {
                // --- SCREEN 1: CATEGORY LIST ---
                LazyColumn {
                    items(items = categories) { cat ->
                        PreferenceCategoryItem(
                            title = cat.title,
                            description = cat.description,
                            icon = cat.icon,
                            onClick = { selectedCategory = cat } // Navigate "deeper"
                        )
                    }
                }
            } else {
                // --- SCREEN 2: SETTINGS CONTENTS ---
                LazyColumn {
                    items(category.items) { item ->
                        when (item) {
                            is PreferenceItem.Action -> ActionPreferenceItem(item)

                            is PreferenceItem.Switch -> {
                                SwitchPreference(
                                    preferenceManager = preferenceManager,
                                    item = item
                                )
                            }

                            is PreferenceItem.Slider -> {
                                SliderPreferenceItem(
                                    preferenceManager = preferenceManager,
                                    item = item
                                )
                            }

                            is PreferenceItem.List -> {
                                ListPreference(
                                    preferenceManager = preferenceManager,
                                    item = item
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}