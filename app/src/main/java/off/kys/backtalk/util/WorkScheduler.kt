package off.kys.backtalk.util

import android.content.Context
import androidx.work.*
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.worker.AutoExportWorker
import off.kys.backtalk.data.worker.ReminderWorker
import off.kys.backtalk.data.worker.UpdateWorker
import java.util.concurrent.TimeUnit

/**
 * Utility for scheduling background tasks using WorkManager.
 */
object WorkScheduler {

    const val REMINDER_WORK_NAME = "daily_reminders_work"
    const val AUTO_EXPORT_WORK_NAME = "auto_export_work"
    const val AUTO_UPDATE_WORK_NAME = "auto_update_work"

    /**
     * Schedules the reminder worker if enabled.
     *
     * @param context The application context.
     * @param preferences The application preferences.
     * @param forceReplace Whether to replace the existing work if it already exists.
     *                     Setting this to true resets the inactivity timer.
     */
    fun scheduleReminders(
        context: Context,
        preferences: BacktalkPreferences,
        forceReplace: Boolean = false
    ) {
        val workManager = WorkManager.getInstance(context)
        
        if (!preferences.remindersEnabled) {
            workManager.cancelUniqueWork(REMINDER_WORK_NAME)
            return
        }

        val interval = preferences.reminderInterval.hours.toLong()
        
        // Basic constraints for reminders to avoid unnecessary battery drain
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(interval, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            // We set an initial delay to avoid bothering the user immediately after setup.
            // When called with forceReplace = true (e.g., on app open), this resets
            // the "inactivity" timer to the full interval.
            .setInitialDelay(interval, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            if (forceReplace) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancels the reminder worker.
     */
    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    /**
     * Schedules the auto-export worker if enabled.
     *
     * @param context The application context.
     * @param preferences The application preferences.
     * @param forceReplace Whether to replace the existing work if it already exists.
     */
    fun scheduleAutoExport(
        context: Context,
        preferences: BacktalkPreferences,
        forceReplace: Boolean = false
    ) {
        val workManager = WorkManager.getInstance(context)
        
        if (!preferences.autoExportEnabled || preferences.autoExportUri == null) {
            workManager.cancelUniqueWork(AUTO_EXPORT_WORK_NAME)
            return
        }

        val interval = preferences.autoRepeatFrequency.hours.toLong()
        
        // Auto-export constraints: battery and storage are critical for reliable backups.
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AutoExportWorker>(
            interval, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            // Start the first export after a short delay to not compete with app startup.
            .setInitialDelay(minOf(interval, 1L), TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            AUTO_EXPORT_WORK_NAME,
            if (forceReplace) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancels the auto-export worker.
     */
    fun cancelAutoExport(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(AUTO_EXPORT_WORK_NAME)
    }

    /**
     * Schedules the auto-update worker if enabled.
     */
    fun scheduleAutoUpdate(
        context: Context,
        preferences: BacktalkPreferences,
        forceReplace: Boolean = false
    ) {
        val workManager = WorkManager.getInstance(context)
        
        if (!preferences.autoUpdateEnabled) {
            workManager.cancelUniqueWork(AUTO_UPDATE_WORK_NAME)
            return
        }

        // Daily update checks are usually sufficient
        val interval = 24L

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<UpdateWorker>(interval, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            // Wait an hour after setup to avoid immediate network requests
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            AUTO_UPDATE_WORK_NAME,
            if (forceReplace) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancels the auto-update worker.
     */
    fun cancelAutoUpdate(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(AUTO_UPDATE_WORK_NAME)
    }
}
