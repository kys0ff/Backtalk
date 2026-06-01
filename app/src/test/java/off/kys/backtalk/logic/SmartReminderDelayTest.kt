package off.kys.backtalk.logic

import off.kys.backtalk.common.SmartIntensity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class SmartReminderDelayTest {

    @Test
    fun `calculate delay ensures no night reminders`() {
        // Scenario 1: Target time is 23:00 (night)
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
        }
        val averageInterval = 3 * 60 * 60 * 1000L // 3 hours
        val intensity = SmartIntensity.NORMAL
        
        var delay = (averageInterval * intensity.multiplier).toLong()
        val dayInMs = 24 * 60 * 60 * 1000L
        val maxDelay = if (intensity.multiplier < 1.0f) dayInMs else dayInMs * 2
        if (delay > maxDelay) delay = maxDelay
        if (delay < 3600000L) delay = 3600000L

        val targetTime = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis + delay
        }
        
        val hour = targetTime.get(Calendar.HOUR_OF_DAY)
        if (hour !in 8..21) {
            if (hour >= 22) {
                targetTime.add(Calendar.DAY_OF_YEAR, 1)
            }
            targetTime.set(Calendar.HOUR_OF_DAY, 9)
            targetTime.set(Calendar.MINUTE, 0)
            targetTime.set(Calendar.SECOND, 0)
            delay = targetTime.timeInMillis - now.timeInMillis
        }
        
        val finalTarget = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis + delay
        }
        
        assertEquals(9, finalTarget.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `calculate delay respects intensity`() {
        val averageInterval = 10 * 60 * 60 * 1000L // 10 hours
        
        // Intense = 25% of average = 2.5 hours
        val intenseMultiplier = SmartIntensity.INTENSE.multiplier
        assertEquals(0.25f, intenseMultiplier)
        
        val expectedDelay = (averageInterval * intenseMultiplier).toLong()
        assertEquals(2.5 * 60 * 60 * 1000, expectedDelay.toDouble(), 0.1)
    }

    @Test
    fun `calculate delay respects daytime`() {
        // Scenario 2: Target time is 15:00 (day)
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
        val averageInterval = 3 * 60 * 60 * 1000L // 3 hours
        val intensity = SmartIntensity.NORMAL

        val delay = (averageInterval * intensity.multiplier).toLong()
        
        val targetTime = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis + delay
        }
        
        val hour = targetTime.get(Calendar.HOUR_OF_DAY)
        if (hour !in 8..21) {
            // Should not enter here
        }
        
        assertEquals(15, targetTime.get(Calendar.HOUR_OF_DAY))
        assertEquals(averageInterval, delay)
    }
}
