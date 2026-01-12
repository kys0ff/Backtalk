package off.kys.github_app_updater.model.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GitHubRelease(
    @param:Json(name = "tag_name")
    val tagName: String,
    val body: String?,
    val assets: List<GitHubAsset>
)