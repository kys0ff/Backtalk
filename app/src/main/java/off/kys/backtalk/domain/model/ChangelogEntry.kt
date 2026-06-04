package off.kys.backtalk.domain.model

/**
 * Data representation of a changelog entry.
 *
 * @property hash The commit hash associated with the entry.
 * @property type The conventional commit type (e.g., feat, fix).
 * @property message The descriptive message of the change.
 * @property isParsedSuccessfully Indicates if the line was successfully matched against the expected format.
 */
data class ChangelogEntry(
    val hash: String,
    val type: String,
    val message: String,
    val isParsedSuccessfully: Boolean = true
)