package off.kys.github_app_updater.model.github

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GitHubRelease(
    val tag_name: String,
    val body: String?,
    val assets: List<GitHubAsset>
)