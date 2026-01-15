package off.kys.github_app_updater.model.updater

import off.kys.github_app_updater.model.github.GitHubAsset

/**
 * Represents the result of an update check.
 *
 * @param latestVersion The latest version of the app.
 * @param changeLog The changelog for the update.
 * @param downloadUrls The download URLs for the update.
 */
data class UpdateResult(
    val latestVersion: String,
    val changeLog: String,
    val downloadUrls: List<GitHubAsset>
)