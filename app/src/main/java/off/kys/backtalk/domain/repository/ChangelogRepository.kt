package off.kys.backtalk.domain.repository

import off.kys.backtalk.domain.model.ChangelogEntry

/**
 * Interface for loading changelog data.
 */
interface ChangelogRepository {
    /**
     * Returns a list of parsed changelog entries.
     */
    fun getChangelogEntries(): List<ChangelogEntry>
}
