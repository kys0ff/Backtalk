package off.kys.backtalk.domain.use_case

import off.kys.backtalk.BuildConfig
import off.kys.backtalk.common.Constants
import off.kys.github_app_updater_lib.checkAppUpdate
import off.kys.github_app_updater_lib.common.ChangelogSource
import off.kys.github_app_updater_lib.model.updater.UpdateResult

/**
 * Use case to check for application updates.
 *
 * This use case uses the `github_app_updater_lib` to check for new releases on GitHub.
 * It skips the check if the app is an F-Droid build, as F-Droid manages its own updates.
 */
class CheckAppUpdate(
    private val currentVersion: String = BuildConfig.VERSION_NAME,
    private val isFDroid: Boolean = BuildConfig.IS_FDROID
) {

    /**
     * Checks for updates and invokes the appropriate callback.
     *
     * @param onUpdateAvailable Callback triggered when a newer version is available on GitHub.
     *                          Provides an [UpdateResult] with update details.
     * @param onUpToDate Callback triggered if the current version is up-to-date or if the check is skipped.
     */
    suspend operator fun invoke(
        onUpdateAvailable: (UpdateResult) -> Unit,
        onUpToDate: () -> Unit
    ) {
        if (isFDroid) {
            onUpToDate()
            return
        }

        runCatching {
            checkAppUpdate {
                githubRepo(Constants.BACKTALK_GITHUB_REPO)
                currentVersion(this@CheckAppUpdate.currentVersion)
                changelogSource(ChangelogSource.COMMITS)

                onUpdateAvailable { result ->
                    onUpdateAvailable(result)
                }
                onUpToDate {
                    onUpToDate()
                }
            }
        }.onFailure {
            onUpToDate()
        }
    }
}