package off.kys.backtalk.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.biometric.BiometricManager
import off.kys.backtalk.R

/**
 * Displays a short [Toast] message.
 *
 * This is an extension function on [Context] that simplifies showing a toast.
 *
 * @param message The text to show. Can be formatted text.
 * @param duration How long to display the message. Can be [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG].
 *                 Defaults to [Toast.LENGTH_SHORT].
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

/**
 * Copies the given text to the system clipboard.
 *
 * For Android 13 (API 33) and above, the system provides a standard visual confirmation
 * when content is added to the clipboard. For older versions (API 32 and below),
 * this function displays a Toast message to confirm the copy action.
 *
 * @receiver The Context used to access system services and resources.
 * @param text The string to be copied to the clipboard.
 */
fun Context.copyToClipboard(text: String) {
    // 1. Get the Clipboard Manager
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // 2. Create a ClipData object (Label is for accessibility/system use)
    val clip = ClipData.newPlainText(getString(R.string.copied_text), text)

    // 3. Set the clip as the primary clipboard content
    clipboard.setPrimaryClip(clip)

    // Only show Toast for Android 12 (API 32) and below
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        toast(getString(R.string.text_copied_to_clipboard))
    }
}


/**
 * Checks if biometric authentication (like fingerprint or face recognition) is available and enrolled on the device.
 *
 * This function uses `BiometricManager` to determine the biometric capability of the device.
 * It's useful for deciding whether to show a biometric login option to the user.
 *
 * @return `true` if biometric authentication is available and at least one biometric is enrolled, `false` otherwise.
 * @see androidx.biometric.BiometricManager
 */
fun Context.isBiometricAvailable(): Boolean {
    val biometricManager = BiometricManager.from(this)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    return when (biometricManager.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }
}