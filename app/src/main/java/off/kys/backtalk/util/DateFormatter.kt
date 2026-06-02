package off.kys.backtalk.util

import android.content.Context
import android.text.format.DateFormat
import off.kys.backtalk.common.AppDateFormat
import off.kys.backtalk.common.AppTimeFormat
import off.kys.backtalk.common.pref.BacktalkPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for formatting dates and times based on user preferences.
 */
class DateFormatter(
    private val context: Context,
    private val preferences: BacktalkPreferences
) {
    /**
     * Formats a timestamp into a date string based on the user's preferred date format.
     */
    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = preferences.dateFormat
        val pattern = when (format) {
            AppDateFormat.SYSTEM -> return DateFormat.getDateFormat(context).format(date)
            AppDateFormat.CUSTOM -> preferences.customDateFormat
            else -> format.pattern ?: "MMM d, yyyy"
        }

        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(date)
        } catch (e: Exception) {
            // Fallback for invalid custom patterns
            DateFormat.getDateFormat(context).format(date)
        }
    }

    /**
     * Formats a timestamp into a time string based on the user's preferred time format.
     */
    fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = preferences.timeFormat
        
        val pattern = when (format) {
            AppTimeFormat.SYSTEM -> return DateFormat.getTimeFormat(context).format(date)
            AppTimeFormat.TWELVE_HOUR -> "h:mm a"
            AppTimeFormat.TWENTY_FOUR_HOUR -> "HH:mm"
        }

        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(date)
        } catch (e: Exception) {
            DateFormat.getTimeFormat(context).format(date)
        }
    }

    /**
     * Formats a timestamp into a combined date and time string.
     */
    fun formatDateTime(timestamp: Long): String {
        return "${formatDate(timestamp)}, ${formatTime(timestamp)}"
    }
    
    /**
     * Formats a timestamp for the message bubble (short time).
     */
    fun formatMessageTime(timestamp: Long): String {
        return formatTime(timestamp)
    }

    /**
     * Formats a timestamp for the thread header (short date).
     */
    fun formatThreadDate(timestamp: Long): String {
        // We might want a specialized "short" date for threads, but for now use the default
        return formatDate(timestamp)
    }
}
