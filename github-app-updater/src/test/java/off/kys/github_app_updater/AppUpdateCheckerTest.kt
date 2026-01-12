package off.kys.github_app_updater

import kotlinx.coroutines.runBlocking
import org.junit.Test

class AppUpdateCheckerTest {

    @Test
    fun testUpdateChecker() = runBlocking {
        AppUpdateChecker.check {
            githubRepo("kys0ff/kli")
            currentVersion("0.1.6")
            onUpdateAvailable { result ->
                println(
                    """
                    Update available!
                    
                    Latest version: ${result.latestVersion}
                    Changelog:
                    ${result.changeLog}
                    Download URL: ${result.downloadUrl}
                    """.trimIndent()
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