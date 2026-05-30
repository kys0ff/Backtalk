package off.kys.backtalk.common

import java.util.concurrent.TimeUnit

/**
 * Global constants used throughout the Backtalk application.
 */
object Constants {

    /**
     * The time gap required between messages to display a new date/time header in the chat UI.
     */
    val TIME_GAP_FOR_HEADER = TimeUnit.HOURS.toMillis(1)

    /**
     * The time gap allowed between messages from the same sender to group them together visually.
     */
    val TIME_GAP_FOR_GROUPING = TimeUnit.MINUTES.toMillis(1)

    /**
     * The time window during which a message can be edited or deleted.
     */
    val MESSAGE_EDIT_DELETE_WINDOW = TimeUnit.HOURS.toMillis(1)

}