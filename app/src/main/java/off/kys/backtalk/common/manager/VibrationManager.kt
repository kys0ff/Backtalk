package off.kys.backtalk.common.manager

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manager class responsible for handling device vibrations and haptic feedback.
 *
 * It abstracts the differences between various Android API levels for vibration,
 * providing a unified interface for simple vibration tasks.
 *
 * @param context The application context used to retrieve the vibration service.
 */
class VibrationManager(context: Context) {

    /**
     * The [Vibrator] instance used to perform vibration operations.
     * It handles the selection of the default vibrator based on the Android version.
     */
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Vibrates the device for a specific duration.
     *
     * On Android O (API 26) and above, it uses [VibrationEffect.createOneShot] with [VibrationEffect.DEFAULT_AMPLITUDE].
     * On older versions, it uses the deprecated [Vibrator.vibrate] method.
     *
     * @param duration The duration of the vibration in milliseconds. Defaults to 500ms.
     */
    fun vibrate(duration: Long = 500) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

}
