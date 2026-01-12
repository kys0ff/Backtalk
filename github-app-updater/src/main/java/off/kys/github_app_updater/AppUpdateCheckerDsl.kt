package off.kys.github_app_updater

import off.kys.github_app_updater.core.UpdateConfig

/**
 * Checks for app updates.
 *
 * @param block Configuration block for the update check.
 */
suspend fun checkAppUpdate(block: UpdateConfig.() -> Unit) {
    AppUpdateChecker.check(block)
}
