package off.kys.backtalk.presentation.state.changelog

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import off.kys.backtalk.domain.model.ChangelogEntry

/**
 * UI state for the changelog.
 */
data class ChangelogUiState(
    val entries: PersistentList<ChangelogEntry> = persistentListOf(),
    val isLoading: Boolean = false
)
