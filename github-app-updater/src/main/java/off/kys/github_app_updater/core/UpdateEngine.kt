package off.kys.github_app_updater.core

import off.kys.github_app_updater.api.GitHubApi
import off.kys.github_app_updater.common.ChangelogSource
import off.kys.github_app_updater.model.github.GitHubCommit
import off.kys.github_app_updater.model.updater.UpdateResult
import off.kys.github_app_updater.util.Version
import off.kys.github_app_updater.util.normalize
import off.kys.github_app_updater.util.normalizeTag

/**
 * Entry point for app update checking.
 */
object UpdateEngine {

    /**
     * Checks for app updates.
     *
     * @param config Configuration for the update check.
     */
    suspend fun check(config: UpdateConfig) {
        val api = GitHubApi.create(config.token)

        val release = api.latestRelease(config.repo)
        val latestVersion = release.tag_name.normalize()

        if (Version(latestVersion) <= Version(config.currentVersion)) {
            config.onUpToDate()
            return
        }

        // Fetch commits as fallback
        val commits = fetchAllCommits(
            api,
            config.repo,
            config.currentVersion.normalizeTag(),
            release.tag_name
        )

        val changelog = when (config.changelogSource) {
            ChangelogSource.RELEASE_BODY -> release.body?.takeIf { it.isNotBlank() }
                ?: commits.joinToString("\n") { "• ${it.commit.message.lineSequence().first()}" }

            ChangelogSource.COMMITS -> commits.joinToString("\n") { "• ${it.commit.message.lineSequence().first()}" }
        }

        // Pick APK asset
        val asset = release.assets.firstOrNull {
            it.name.endsWith(".apk", ignoreCase = true) ||
                    it.browser_download_url.endsWith(".apk", ignoreCase = true)
        }

        config.onUpdateAvailable(
            UpdateResult(
                latestVersion = latestVersion,
                changeLog = changelog,
                downloadUrl = asset?.browser_download_url.orEmpty()
            )
        )
    }

    /**
     * Fetches all commits between two versions.
     *
     * @param api The GitHub API instance.
     * @param repo The repository name.
     * @param base The base version.
     * @param head The head version.
     * @return List of GitHub commits.
     */
    private suspend fun fetchAllCommits(
        api: GitHubApi,
        repo: String,
        base: String,
        head: String
    ): List<GitHubCommit> {
        val response = api.compare(repo, base, head)
        return response.commits
    }
}