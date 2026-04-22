package off.kys.backtalk.domain.use_case

import off.kys.backtalk.BuildConfig
import off.kys.github_app_updater_lib.checkAppUpdate
import off.kys.github_app_updater_lib.common.ChangelogSource
import off.kys.github_app_updater_lib.model.updater.UpdateResult

class CheckAppUpdate {

    suspend operator fun invoke(
        onUpdateAvailable: (UpdateResult) -> Unit,
        onUpToDate: () -> Unit
    ) {

        if (BuildConfig.IS_FDROID) {
            onUpToDate()
            return
        }

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