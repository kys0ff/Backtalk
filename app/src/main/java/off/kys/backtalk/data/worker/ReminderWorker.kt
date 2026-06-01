package off.kys.backtalk.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import off.kys.backtalk.R
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.activity.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val CHANNEL_ID = "daily_reminders_channel"
private const val NOTIFICATION_ID = 1001

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val preferences: BacktalkPreferences by inject()

    override suspend fun doWork(): Result {
        if (!preferences.remindersEnabled) return Result.success()

        // If they ignored the last notification, check how old it is.
        if (preferences.hasUnreadReminder) {
            val lastReminder = preferences.lastReminderTimestamp
            val twelveHoursInMs = 12 * 60 * 60 * 1000L
            
            // If the unread notification is less than 12 hours old, skip.
            // This prevents spamming multiple notifications within a short window,
            // but ensures the user eventually gets reminded again if they just ignored it.
            if (System.currentTimeMillis() - lastReminder < twelveHoursInMs) {
                return Result.success()
            }
        }

        showNotification(applicationContext)

        // Mark that a notification is now active/unread
        preferences.hasUnreadReminder = true
        preferences.lastReminderTimestamp = System.currentTimeMillis()

        if (preferences.reminderInterval == ExportInterval.SMART) {
            scheduleSmartReminder(applicationContext)
        }

        return Result.success()
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val messages = listOf(
            R.string.reminder_message_1, R.string.reminder_message_2, R.string.reminder_message_3,
            R.string.reminder_message_4, R.string.reminder_message_5, R.string.reminder_message_6,
            R.string.reminder_message_7, R.string.reminder_message_8, R.string.reminder_message_9,
            R.string.reminder_message_10, R.string.reminder_message_11, R.string.reminder_message_12,
            R.string.reminder_message_13, R.string.reminder_message_14
        )
        val randomMessage = context.getString(messages[Random.nextInt(messages.size)])

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.round_send_24)
            .setContentTitle(context.getString(R.string.reminder_message_title))
            .setContentText(randomMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object : KoinComponent {
        private const val SMART_REMINDER_WORK_NAME = "smart_reminder_work"

        fun scheduleSmartReminder(context: Context) {
            val preferences: BacktalkPreferences = get()

            val intensity = preferences.smartReminderIntensity
            var delay = (preferences.averageUsageInterval * intensity.multiplier).toLong()
            
            val dayInMs = 24 * 60 * 60 * 1000L
            // Cap at 24h for normal/relaxed, but allow more frequent if intense/frequent
            val maxDelay = if (intensity.multiplier < 1.0f) dayInMs else dayInMs * 2
            
            if (delay > maxDelay) {
                delay = maxDelay
            }
            // Minimum delay of 1 hour to avoid any weird spamming
            if (delay < 60 * 60 * 1000L) {
                delay = 60 * 60 * 1000L
            }

            val now = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis + delay
            }

            // FIX 2: Clear minutes/seconds/millis to make quiet zone adjustments predictable
            val hour = targetTime.get(Calendar.HOUR_OF_DAY)
            if (hour !in 8..21) {
                if (hour >= 22) {
                    // It landed late at night. Push it to 9 AM the next day.
                    targetTime.add(Calendar.DAY_OF_YEAR, 1)
                } // If it's early morning (0-7), it's already the correct day.

                targetTime.set(Calendar.HOUR_OF_DAY, 9)
                targetTime.set(Calendar.MINUTE, 0)
                targetTime.set(Calendar.SECOND, 0)
                targetTime.set(Calendar.MILLISECOND, 0)

                delay = targetTime.timeInMillis - now.timeInMillis
            }

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()

            // FIX 3: Keeps REPLACE policy safe because MainActivity will clear 
            // `hasUnreadReminder` and re-schedule this cleanly when the app opens.
            WorkManager.getInstance(context).enqueueUniqueWork(
                SMART_REMINDER_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun cancelSmartReminder(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SMART_REMINDER_WORK_NAME)
        }
    }
}
