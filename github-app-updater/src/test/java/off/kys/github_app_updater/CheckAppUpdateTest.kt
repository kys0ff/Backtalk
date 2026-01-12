package off.kys.github_app_updater

import kotlinx.coroutines.runBlocking
import off.kys.github_app_updater.common.ChangelogSource
import org.junit.Test

class CheckAppUpdateTest {

    @Test
    fun testCheckAppUpdate() = runBlocking {
        checkAppUpdate {
            githubRepo("kys0ff/kli")
            currentVersion("0.1.0")
            changelogSource(ChangelogSource.COMMITS)

            onUpdateAvailable { result ->
                println(
                    """
                    Update available!
                    
                    Latest version: ${result.latestVersion}
                    Changelog:
                    ${result.changeLog}
                    Download URL: ${result.downloadUrls}
                    """.trimIndent().trimMargin()
                )
            }
            onUpToDate {
                println(
                    """
                        The app is up to date!
                    """.trimIndent()
                )
            }
        }
    }

}