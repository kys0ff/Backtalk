package off.kys.github_app_updater.core

import off.kys.github_app_updater.model.updater.UpdateResult

/**
 * Configuration for app update checking.
 */
class UpdateConfig {
    /**
     * The GitHub repository to check for updates.
     */
    lateinit var repo: String

    /**
     * The current version of the app.
     */
    lateinit var currentVersion: String

    /**
     * The GitHub personal access token for authentication.
     */
    var token: String? = null

    /**
     * Callback when an update is available.
     */
    var onUpdateAvailable: (UpdateResult) -> Unit = {}

    /**
     * Callback when the app is up to date.
     */
    var onUpToDate: () -> Unit = {}

    /**
     * Sets the GitHub repository.
     *
     * @param value The repository name.
     */
    fun githubRepo(value: String) { repo = value }

    /**
     * Sets the current version of the app.
     *
     * @param value The current version.
     */
    fun currentVersion(value: String) { currentVersion = value }

    /**
     * Sets the GitHub personal access token.
     *
     * @param value The token value.
     */
    fun githubToken(value: String?) { token = value }

    /**
     * Sets the callback when an update is available.
     *
     * @param block The callback function.
     */
    fun onUpdateAvailable(block: (UpdateResult) -> Unit) {
        onUpdateAvailable = block
    }

    /**
     * Sets the callback when the app is up to date.
     *
     * @param block The callback function.
     */
    fun onUpToDate(block: () -> Unit) {
        onUpToDate = block
    }
}