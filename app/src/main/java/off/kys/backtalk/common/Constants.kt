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
     * The raw URL to the MIT license for the Backtalk project on GitHub.
     */
    const val BACKTALK_MIT_LICENSE_RAW_URL: String = "https://raw.githubusercontent.com/kys0ff/Backtalk/refs/heads/master/LICENSE"

}