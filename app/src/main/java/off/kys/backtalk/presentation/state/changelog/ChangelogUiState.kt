package off.kys.backtalk.presentation.state.changelog

import off.kys.backtalk.domain.model.ChangelogEntry

/**
 * UI state for the changelog.
 */
data class ChangelogUiState(
    val entries: List<ChangelogEntry> = emptyList(),
    val isLoading: Boolean = false
)