package off.kys.backtalk.common

/**
 * Global constants used throughout the Backtalk application.
 */
object Constants {

    const val BACKTALK_GITHUB_REPO: String = "kys0ff/Backtalk"

    /**
     * The ID for the daily reminders notification channel.
     */
    const val REMINDER_CHANNEL_ID: String = "daily_reminders_channel"

    /**
     * The unique ID for the reminder notification.
     */
    const val REMINDER_NOTIFICATION_ID: Int = 1001

    /**
     * The time gap required between messages to display a new date/time header in the chat UI.
     */
    const val TIME_GAP_FOR_HEADER: Long = 3600000L // 1 Hour

    /**
     * The time gap allowed between messages from the same sender to group them together visually.
     */
    const val TIME_GAP_FOR_GROUPING: Long = 60000L // 1 Minute

    /**
     * The time window during which a message can be edited or deleted.
     */
    const val MESSAGE_EDIT_DELETE_WINDOW: Long = 3600000L // 1 Hour
}