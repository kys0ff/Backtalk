package off.kys.backtalk.presentation.screen.messages.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.components.HintTooltip
import off.kys.backtalk.util.emptyString

/**
 * Composable function that displays the messages top bar, which dynamically switches between
 * search, selection, and default viewing modes.
 *
 * @param scrollBehavior The scroll behavior of the top bar to handle elevation and color transitions.
 * @param selectedCount The number of messages currently selected.
 * @param isSearchActive Whether the search mode interface is currently active.
 * @param searchQuery The current text query entered in the search field.
 * @param searchResultsCount The total number of search results found.
 * @param currentSearchIndex The current search result index for navigation.
 * @param onCloseSelection Callback to clear the message selection and return to default state.
 * @param onDelete Callback to handle the deletion of selected messages.
 * @param onCopy Callback to handle copying the content of selected messages.
 * @param onSettings Callback to navigate to the settings screen.
 * @param onThreads Callback to navigate to the threads/conversations screen.
 * @param onStatistics Callback to navigate to the statistics screen.
 * @param onToggleSearch Callback to enable or disable the search mode.
 * @param onSearchQueryChange Callback to update the current search query text.
 * @param onNavigateSearch Callback to move between search results (true for up, false for down).
 * @param tags A list of tag strings available for filtering.
 * @param selectedTag The currently active filter tag, or null if no tag is selected.
 * @param onTagClick Callback triggered when a tag is clicked for filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    selectedCount: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    searchResultsCount: Int,
    currentSearchIndex: Int,
    onCloseSelection: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onPin: () -> Unit,
    onSettings: () -> Unit,
    onThreads: () -> Unit,
    onReminders: () -> Unit,
    onStatistics: () -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigateSearch: (Boolean) -> Unit,
    tags: List<String>,
    selectedTag: String?,
    onTagClick: (String) -> Unit,
    isImageSelectionOnly: Boolean = false,
    canDelete: Boolean = true
) {
    val appBarColors = TopAppBarDefaults.topAppBarColors()
    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val containerColor = lerp(
        appBarColors.containerColor,
        appBarColors.scrolledContainerColor,
        colorTransitionFraction
    )

    Column(Modifier.background(containerColor)) {
        val transparentTopAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )

        when {
            isSearchActive -> {
                SearchTopBar(
                    searchQuery = searchQuery,
                    searchResultsCount = searchResultsCount,
                    currentSearchIndex = currentSearchIndex,
                    onSearchQueryChange = onSearchQueryChange,
                    onNavigateSearch = onNavigateSearch,
                    onCloseSearch = { onToggleSearch(false) },
                    scrollBehavior = scrollBehavior,
                    colors = transparentTopAppBarColors
                )
            }

            selectedCount > 0 -> {
                SelectionTopBar(
                    selectedCount = selectedCount,
                    onCloseSelection = onCloseSelection,
                    onDelete = onDelete,
                    onCopy = onCopy,
                    onPin = onPin,
                    scrollBehavior = scrollBehavior,
                    colors = transparentTopAppBarColors,
                    showPin = !isImageSelectionOnly && selectedCount == 1,
                    showCopy = !isImageSelectionOnly,
                    canDelete = canDelete
                )
            }

            else -> {
                DefaultTopBar(
                    onToggleSearch = { onToggleSearch(true) },
                    onSettings = onSettings,
                    onThreads = onThreads,
                    onReminders = onReminders,
                    onStatistics = onStatistics,
                    scrollBehavior = scrollBehavior,
                    colors = transparentTopAppBarColors
                )
            }
        }

        AnimatedVisibility(
            visible = selectedCount == 0,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 }
        ) {
            TagFilterBar(
                tags = tags,
                selectedTag = selectedTag,
                onTagClick = onTagClick
            )
        }
    }
}

/**
 * Top bar displayed when the user is actively searching through messages.
 *
 * @param searchQuery The current text entered in the search field.
 * @param searchResultsCount Total number of matches found.
 * @param currentSearchIndex The 0-based index of the currently focused result.
 * @param onSearchQueryChange Callback for text updates.
 * @param onNavigateSearch Callback to move between results (true for up/previous, false for down/next).
 * @param onCloseSearch Callback to exit search mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    searchResultsCount: Int,
    currentSearchIndex: Int,
    onSearchQueryChange: (String) -> Unit,
    onNavigateSearch: (Boolean) -> Unit,
    onCloseSearch: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    colors: androidx.compose.material3.TopAppBarColors
) {
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = {
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.focusRequester(focusRequester),
                maxLines = 1,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
                cursorBrush = SolidColor(LocalContentColor.current),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onNavigateSearch(true)
                    }
                ),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )
        },
        navigationIcon = {
            HintTooltip(stringResource(R.string.common_back)) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        painter = painterResource(R.drawable.round_arrow_back_24),
                        contentDescription = stringResource(R.string.common_back)
                    )
                }
            }
        },
        actions = {
            if (searchQuery.isNotEmpty()) {
                SearchActions(
                    searchResultsCount = searchResultsCount,
                    currentSearchIndex = currentSearchIndex,
                    onClearQuery = { onSearchQueryChange(emptyString()) },
                    onNavigateSearch = onNavigateSearch
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = colors
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

/**
 * Encapsulates the action buttons and result counter for the search mode.
 */
@Composable
private fun SearchActions(
    searchResultsCount: Int,
    currentSearchIndex: Int,
    onClearQuery: () -> Unit,
    onNavigateSearch: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HintTooltip(stringResource(R.string.common_clear)) {
            IconButton(onClick = onClearQuery) {
                Icon(
                    painter = painterResource(R.drawable.round_close_24),
                    contentDescription = stringResource(R.string.common_clear)
                )
            }
        }
        Text(
            text = if (searchResultsCount > 0) {
                stringResource(
                    R.string.search_results_count,
                    currentSearchIndex + 1,
                    searchResultsCount
                )
            } else {
                stringResource(R.string.search_no_results)
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HintTooltip(stringResource(R.string.search_previous)) {
            IconButton(onClick = { onNavigateSearch(true) }, enabled = searchResultsCount > 0) {
                Icon(
                    painter = painterResource(R.drawable.round_expand_less_24),
                    contentDescription = stringResource(R.string.search_previous)
                )
            }
        }
        HintTooltip(stringResource(R.string.search_next)) {
            IconButton(onClick = { onNavigateSearch(false) }, enabled = searchResultsCount > 0) {
                Icon(
                    painter = painterResource(R.drawable.round_expand_more_24),
                    contentDescription = stringResource(R.string.search_next)
                )
            }
        }
    }
}

/**
 * Top bar displayed when one or more messages are selected.
 *
 * @param selectedCount Number of items currently selected.
 * @param onCloseSelection Callback to clear selection and return to default state.
 * @param onDelete Callback to delete selected items.
 * @param onCopy Callback to copy selected items to clipboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onCloseSelection: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onPin: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    colors: androidx.compose.material3.TopAppBarColors,
    showPin: Boolean,
    showCopy: Boolean,
    canDelete: Boolean = true
) {
    TopAppBar(
        title = { Text(stringResource(R.string.chat_selection_count, selectedCount)) },
        navigationIcon = {
            HintTooltip(stringResource(R.string.common_close)) {
                IconButton(onClick = onCloseSelection) {
                    Icon(
                        painter = painterResource(R.drawable.round_close_24),
                        contentDescription = stringResource(R.string.common_close)
                    )
                }
            }
        },
        actions = {
            if (showPin) {
                HintTooltip("Pin") {
                    IconButton(onClick = onPin) {
                        Icon(
                            painter = painterResource(R.drawable.round_push_pin_24),
                            contentDescription = "Pin",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            HintTooltip(if (canDelete) stringResource(R.string.common_delete) else "Cannot delete messages older than 1 hour") {
                IconButton(onClick = onDelete, enabled = canDelete) {
                    Icon(
                        painter = painterResource(R.drawable.round_delete_24),
                        contentDescription = stringResource(R.string.common_delete),
                        tint = if (canDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (showCopy) {
                HintTooltip(stringResource(R.string.common_copy)) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            painter = painterResource(R.drawable.round_content_copy_24),
                            contentDescription = stringResource(R.string.common_copy)
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = colors
    )
}

/**
 * The standard top bar shown during normal viewing.
 *
 * @param onToggleSearch Callback to enter search mode.
 * @param onSettings Callback to navigate to settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopBar(
    onToggleSearch: () -> Unit,
    onSettings: () -> Unit,
    onThreads: () -> Unit,
    onReminders: () -> Unit,
    onStatistics: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    colors: androidx.compose.material3.TopAppBarColors
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.chat_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            HintTooltip(stringResource(R.string.common_search)) {
                IconButton(onClick = onToggleSearch) {
                    Icon(
                        painter = painterResource(R.drawable.round_search_24),
                        contentDescription = stringResource(R.string.common_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                HintTooltip(stringResource(R.string.common_more)) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            painter = painterResource(R.drawable.round_more_vert_24),
                            contentDescription = stringResource(R.string.common_more),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(24.dp))) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .widthIn(min = 180.dp)
                            .graphicsLayer {
                                transformOrigin =
                                    TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0f)
                            },
                    ) {
                        MenuOption(
                            label = stringResource(R.string.threads_title),
                            icon = R.drawable.round_thread_24px,
                            onClick = {
                                showMenu = false
                                onThreads()
                            }
                        )
                        MenuOption(
                            label = stringResource(R.string.reminders_title),
                            icon = R.drawable.round_access_alarm_24,
                            onClick = {
                                showMenu = false
                                onReminders()
                            }
                        )
                        MenuOption(
                            label = stringResource(R.string.statistics_title),
                            icon = R.drawable.round_insert_chart_outlined_24,
                            onClick = {
                                showMenu = false
                                onStatistics()
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        MenuOption(
                            label = stringResource(R.string.settings_title),
                            icon = R.drawable.round_settings_24,
                            onClick = {
                                showMenu = false
                                onSettings()
                            }
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = colors
    )
}

@Composable
private fun MenuOption(
    label: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    )
}
