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
import off.kys.backtalk.domain.use_case.CheckAppUpdate
import off.kys.backtalk.presentation.activity.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Worker that checks for application updates in the background.
 */
class UpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val preferences: BacktalkPreferences by inject()
    private val checkAppUpdate: CheckAppUpdate by inject()

    override suspend fun doWork(): Result {
        if (!preferences.autoUpdateEnabled) return Result.success()

        checkAppUpdate(
            onUpdateAvailable = { result ->
                showNotification(applicationContext, result.latestVersion)
            },
            onUpToDate = {}
        )

        return Result.success()
    }

    private fun showNotification(context: Context, versionName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.UPDATE_CHANNEL_ID,
                context.getString(R.string.settings_auto_check_updates),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.round_update_24)
            .setContentTitle(context.getString(R.string.update_dialog_title))
            .setContentText(context.getString(R.string.update_dialog_message, versionName, ""))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(Constants.UPDATE_NOTIFICATION_ID, notification)
    }
}
