package off.kys.backtalk.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.activity.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val preferences: BacktalkPreferences by inject()

    override suspend fun doWork(): Result {
        if (!preferences.remindersEnabled) return Result.success()

        val lastReminder = preferences.lastReminderTimestamp
        val intervalMillis = preferences.reminderInterval.hours * 60 * 60 * 1000L
        val currentTime = System.currentTimeMillis()

        // Skip if the user has been active or reminded within the interval.
        // This ensures reminders are actually "periodic since last use".
        if (currentTime - lastReminder < intervalMillis) {
            return Result.success()
        }

        showNotification(applicationContext)

        // Update last reminder timestamp
        preferences.lastReminderTimestamp = currentTime

        return Result.success()
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.REMINDER_CHANNEL_ID,
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

        val notification = NotificationCompat.Builder(context, Constants.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.round_send_24)
            .setContentTitle(context.getString(R.string.reminder_message_title))
            .setContentText(randomMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(Constants.REMINDER_NOTIFICATION_ID, notification)
    }
}
