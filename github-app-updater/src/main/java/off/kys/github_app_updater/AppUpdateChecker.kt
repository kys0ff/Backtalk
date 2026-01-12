package off.kys.github_app_updater

import off.kys.github_app_updater.core.UpdateConfig
import off.kys.github_app_updater.core.UpdateEngine

/**
 * Entry point for app update checking.
 */
object AppUpdateChecker {

    /**
     * Checks for app updates.
     *
     * @param block Configuration block for the update check.
     */
    suspend fun check(block: UpdateConfig.() -> Unit) {
        val config = UpdateConfig().apply(block)
        UpdateEngine.check(config)
    }
}
