package off.kys.github_app_updater.model.github

import com.squareup.moshi.Json

/**
 * Represents a GitHub release.
 */
data class GitHubAsset(
    /**
     * The name of the asset.
     */
    val name: String,
    /**
     * The URL to download the asset.
     */
    @param:Json(name = "browser_download_url")
    val browserDownloadUrl: String
)