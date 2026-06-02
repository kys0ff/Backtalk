package off.kys.backtalk.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import off.kys.backtalk.common.pref.BacktalkPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Worker responsible for updating application usage statistics in the background.
 *
 * This worker calculates the moving average of the interval between app uses,
 * which is used to power features like "Smart Reminders".
 */
class UsageStatsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val preferences: BacktalkPreferences by inject()

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val lastUsage = preferences.lastUsageTimestamp

        if (lastUsage != 0L) {
            val interval = now - lastUsage
            // Only count if it's been at least 5 minutes since last usage to avoid spamming stats
            if (interval > 5 * 60 * 1000L) {
                val count = preferences.usageCount
                val avg = preferences.averageUsageInterval

                // Simple moving average calculation
                val newAvg = (avg * count + interval) / (count + 1)

                preferences.averageUsageInterval = newAvg
                preferences.usageCount = count + 1
                preferences.lastUsageTimestamp = now
            }
        } else {
            // First time tracking usage or after a reset
            preferences.lastUsageTimestamp = now
            if (preferences.usageCount == 0) {
                preferences.usageCount = 1
            }
        }

        return Result.success()
    }
}
