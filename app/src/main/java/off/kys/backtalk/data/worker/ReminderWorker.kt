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
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.activity.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

private const val CHANNEL_ID = "daily_reminders_channel"
private const val NOTIFICATION_ID = 1001

/**
 * Worker that shows a reminder notification to the user.
 */
class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val preferences: BacktalkPreferences by inject()

    override suspend fun doWork(): Result {
        if (!preferences.remindersEnabled) return Result.success()

        showNotification(applicationContext)
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
            R.string.reminder_message_1,
            R.string.reminder_message_2,
            R.string.reminder_message_3,
            R.string.reminder_message_4,
            R.string.reminder_message_5,
            R.string.reminder_message_6,
            R.string.reminder_message_7
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
}
