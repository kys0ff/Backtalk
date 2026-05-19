package off.kys.backtalk.common.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.presentation.activity.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val CHANNEL_ID = "scheduled_messages_channel"

/**
 * Receiver that handles delivered scheduled messages.
 */
class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: MessagesRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val messageIdValue = intent.getLongExtra("EXTRA_MESSAGE_ID", -1L)
        if (messageIdValue == -1L) return
        val messageId = MessageId(messageIdValue)

        scope.launch {
            val scheduledMessage = repository.getScheduledMessageById(messageId) ?: return@launch

            // Create real message from scheduled one
            val messageEntity = MessageEntity(
                id = scheduledMessage.id,
                text = scheduledMessage.text,
                timestamp = System.currentTimeMillis(), // Delivery time
                repliedToId = scheduledMessage.repliedToId,
                isReminder = true,
                originalCreationTimestamp = scheduledMessage.creationTimestamp,
                scheduledTimestamp = scheduledMessage.scheduledTimestamp,
                mediaPath = scheduledMessage.mediaPath,
                mediaPaths = scheduledMessage.mediaPaths,
                mediaType = scheduledMessage.mediaType
            )

            // Insert into messages and delete from scheduled
            repository.insertMessage(messageEntity)
            repository.deleteScheduledMessageById(messageId)

            // Show notification
            showNotification(context, messageEntity.text)
        }
    }

    private fun showNotification(context: Context, text: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.chat_reminder_notification_channel),
                NotificationManager.IMPORTANCE_HIGH
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.round_send_24) // Replace with app icon if needed
            .setContentTitle(context.getString(R.string.chat_reminder_notification_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
