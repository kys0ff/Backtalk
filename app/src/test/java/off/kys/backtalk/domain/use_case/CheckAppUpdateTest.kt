package off.kys.backtalk.domain.use_case

import kotlinx.coroutines.test.runTest
import org.junit.Test

class CheckAppUpdateTest {

    @Test
    fun checkAppUpdate() = runTest {
        val useCase = CheckAppUpdate(currentVersion = "0.1.3", isFDroid = false)
        var updateAvailable = false
        
        useCase(
            onUpdateAvailable = {
                updateAvailable = true
                println("SUCCESS: $it")
            },
            onUpToDate = {
                updateAvailable = false
                println("FAILED")
            }
        )
        
        assert(updateAvailable)
    }
}
