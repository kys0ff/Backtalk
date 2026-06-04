package off.kys.backtalk.data.repository

import android.content.Context
import off.kys.backtalk.domain.model.ChangelogEntry
import off.kys.backtalk.domain.repository.ChangelogRepository
import off.kys.backtalk.util.emptyString

class ChangelogRepositoryImpl(
    private val context: Context
) : ChangelogRepository {

    override fun getChangelogEntries(): List<ChangelogEntry> {
        return try {
            context.assets.open("changelog.txt").bufferedReader().use { reader ->
                reader.lineSequence()
                    .filter { it.isNotBlank() }
                    .flatMap { parseChangelogLine(it) }
                    .toList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseChangelogLine(line: String): List<ChangelogEntry> {
        val firstSpace = line.indexOf(' ')
        if (firstSpace == -1) {
            return listOf(
                ChangelogEntry(
                    emptyString(),
                    emptyString(),
                    line,
                    isParsedSuccessfully = false
                )
            )
        }

        val hash = line.substring(0, firstSpace)
        val remainder = line.substring(firstSpace + 1).trim()

        // Handle Merge Pull Request
        val mergeRegex = Regex("""^Merge pull request (#\d+) from (.*)$""", RegexOption.IGNORE_CASE)
        val mergeMatch = mergeRegex.find(remainder)
        if (mergeMatch != null) {
            val prNumber = mergeMatch.groupValues[1]
            val branch = mergeMatch.groupValues[2]
            return listOf(
                ChangelogEntry(
                    hash = hash,
                    type = "merge",
                    message = "Pull request $prNumber from $branch"
                )
            )
        }

        // Handle generic Merge commits
        if (remainder.startsWith("Merge ", ignoreCase = true)) {
            return listOf(
                ChangelogEntry(
                    hash = hash,
                    type = "merge",
                    message = remainder.removePrefix("Merge ").removePrefix("merge ").trim()
                )
            )
        }

        val regex = Regex("""\b(feat|fix|refactor|chore|docs|style|ui|build)(?:\s+(#\d+))?:\s+""")
        val matches = regex.findAll(remainder).toList()

        if (matches.isEmpty()) {
            return listOf(
                ChangelogEntry(
                    hash = hash,
                    type = emptyString(),
                    message = remainder,
                    isParsedSuccessfully = false
                )
            )
        }

        val entries = mutableListOf<ChangelogEntry>()

        for (i in matches.indices) {
            val currentMatch = matches[i]
            val type = currentMatch.groupValues[1].lowercase()
            val issue = currentMatch.groupValues[2]

            val messageStart = currentMatch.range.last + 1
            val messageEnd = if (i + 1 < matches.size) {
                matches[i + 1].range.first
            } else {
                remainder.length
            }

            val rawMessage = remainder.substring(messageStart, messageEnd).trim()
            val message = if (issue.isNotEmpty()) "$issue $rawMessage" else rawMessage
            entries.add(ChangelogEntry(hash = hash, type = type, message = message))
        }

        return entries
    }
}
