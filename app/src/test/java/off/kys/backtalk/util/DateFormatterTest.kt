package off.kys.backtalk.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import off.kys.backtalk.common.AppDateFormat
import off.kys.backtalk.common.AppTimeFormat
import off.kys.backtalk.common.pref.BacktalkPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DateFormatterTest {

    private lateinit var context: Context
    private lateinit var preferences: BacktalkPreferences
    private lateinit var dateFormatter: DateFormatter

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        preferences = mockk(relaxed = true)
        dateFormatter = DateFormatter(context, preferences)
        
        // Mock Locale.getDefault() if needed, but SimpleDateFormat uses it by default.
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `formatDate with DMY_SLASH returns correct format`() {
        every { preferences.dateFormat } returns AppDateFormat.DMY_SLASH
        val timestamp = 1717326000000L // June 2, 2024
        
        val result = dateFormatter.formatDate(timestamp)
        assertEquals("02/06/2024", result)
    }

    @Test
    fun `formatDate with YMD_DASH returns correct format`() {
        every { preferences.dateFormat } returns AppDateFormat.YMD_DASH
        val timestamp = 1717326000000L // June 2, 2024
        
        val result = dateFormatter.formatDate(timestamp)
        assertEquals("2024-06-02", result)
    }

    @Test
    fun `formatDate with CUSTOM returns correct format`() {
        every { preferences.dateFormat } returns AppDateFormat.CUSTOM
        every { preferences.customDateFormat } returns "EEEE, MMM d"
        val timestamp = 1717326000000L // June 2, 2024
        
        val result = dateFormatter.formatDate(timestamp)
        assertEquals("Sunday, Jun 2", result)
    }

    @Test
    fun `formatTime with TWELVE_HOUR returns correct format`() {
        every { preferences.timeFormat } returns AppTimeFormat.TWELVE_HOUR
        // Use a fixed date to avoid time zone issues if possible, or just check the format
        val timestamp = 1717326000000L + (2 * 3600 * 1000) + (30 * 60 * 1000) // 02:30 UTC
        
        val result = dateFormatter.formatTime(timestamp)
        // result will be "2:30 AM" or similar depending on test environment TZ
        assert(result.contains(":30"))
        assert(result.contains("AM") || result.contains("PM"))
    }

    @Test
    fun `formatTime with TWENTY_FOUR_HOUR returns correct format`() {
        every { preferences.timeFormat } returns AppTimeFormat.TWENTY_FOUR_HOUR
        val timestamp = 1717326000000L + (2 * 3600 * 1000) + (30 * 60 * 1000) // 02:30 UTC
        
        val result = dateFormatter.formatTime(timestamp)
        assert(result.contains(":30"))
        assert(!result.contains("AM") && !result.contains("PM"))
    }
}
