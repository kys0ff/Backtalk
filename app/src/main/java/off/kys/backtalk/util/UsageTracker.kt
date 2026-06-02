package off.kys.backtalk.util

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import off.kys.backtalk.data.worker.UsageStatsWorker

/**
 * Tracks application lifecycle events to trigger usage statistics updates.
 *
 * This observer listens for the app coming to the foreground and enqueues
 * a [UsageStatsWorker] to update usage metrics in a battery-friendly way.
 */
class UsageTracker(private val context: Context) : DefaultLifecycleObserver {

    /**
     * Called when the application process comes to the foreground.
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        
        val workRequest = OneTimeWorkRequestBuilder<UsageStatsWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
