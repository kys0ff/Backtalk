package off.kys.github_app_updater.model.updater

import off.kys.github_app_updater.model.github.GitHubAsset

data class UpdateResult(
    val latestVersion: String,
    val changeLog: String,
    val downloadUrls: List<GitHubAsset>
)