package off.kys.github_app_updater.model.github

import com.squareup.moshi.Json

data class GitHubAsset(
    val name: String,
    @param:Json(name = "browser_download_url")
    val browserDownloadUrl: String
)