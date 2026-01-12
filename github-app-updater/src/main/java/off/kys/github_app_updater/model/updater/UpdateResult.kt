package off.kys.github_app_updater.model.updater

data class UpdateResult(
    val latestVersion: String,
    val changeLog: String,
    val downloadUrl: String
)