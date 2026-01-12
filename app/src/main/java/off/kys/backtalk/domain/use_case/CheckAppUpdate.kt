package off.kys.backtalk.domain.use_case

import off.kys.backtalk.BuildConfig
import off.kys.github_app_updater.checkAppUpdate
import off.kys.github_app_updater.common.ChangelogSource
import off.kys.github_app_updater.model.updater.UpdateResult

class CheckAppUpdate {

    suspend operator fun invoke(
        onUpdateAvailable: (UpdateResult) -> Unit,
        onUpToDate: () -> Unit
    ) {
        checkAppUpdate {
            githubRepo("kys0ff/Backtalk")
            currentVersion(BuildConfig.VERSION_NAME)
            changelogSource(ChangelogSource.COMMITS)

            onUpdateAvailable { result ->
                onUpdateAvailable(result)
            }
            onUpToDate {
                onUpToDate()
            }
        }
    }

}