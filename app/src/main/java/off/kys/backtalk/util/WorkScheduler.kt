package off.kys.backtalk.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.worker.AutoExportWorker
import off.kys.backtalk.data.worker.ReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Utility for scheduling background tasks using WorkManager.
 */
object WorkScheduler {

    const val REMINDER_WORK_NAME = "daily_reminders_work"
    const val AUTO_EXPORT_WORK_NAME = "auto_export_work"

    /**
     * Schedules the reminder worker if enabled.
     *
     * @param context The application context.
     * @param preferences The application preferences.
     * @param forceReplace Whether to replace the existing work if it already exists.
     */
    fun scheduleReminders(
        context: Context,
        preferences: BacktalkPreferences,
        forceReplace: Boolean = false
    ) {
        if (!preferences.remindersEnabled) {
            cancelReminders(context)
            return
        }

        val interval = preferences.reminderInterval.hours.toLong()
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(interval, TimeUnit.HOURS)
            .setInitialDelay(interval, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
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
     */
    fun scheduleAutoExport(
        context: Context,
        preferences: BacktalkPreferences,
        forceReplace: Boolean = false
    ) {
        if (!preferences.autoExportEnabled || preferences.autoExportUri == null) {
            cancelAutoExport(context)
            return
        }

        val interval = preferences.autoRepeatFrequency
        val workRequest = PeriodicWorkRequestBuilder<AutoExportWorker>(
            interval.hours.toLong(), TimeUnit.HOURS
        )
            .setInitialDelay(interval.hours.toLong(), TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
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
}
