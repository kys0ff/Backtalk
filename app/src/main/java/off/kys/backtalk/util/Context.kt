package off.kys.backtalk.util

import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.core.net.toUri
import off.kys.backtalk.R

/**
 * Displays a short [Toast] message.
 *
 * This is an extension function on [Context] that simplifies showing a toast.
 *
 * @param message The text to show. Can be formatted text.
 * @param duration How long to display the message. Can be [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG].
 *                 Defaults to [Toast.LENGTH_SHORT].
 *
 * @receiver The Context used to access system services and resources.
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

/**
 * Displays a short [Toast] message.
 *
 * This is an extension function on [Context] that simplifies showing a toast.
 *
 * @param message The resource ID of the string to show.
 * @param duration How long to display the message. Can be [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG].
 *                 Defaults to [Toast.LENGTH_SHORT].
 *
 * @receiver The Context used to access system services and resources.
 */
fun Context.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

/**
 * Opens the given URL in the default browser.
 *
 * @param url The URL to open.
 * @receiver The Context used to access system services and resources.
 */
fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    startActivity(intent)
}

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
    val clip = ClipData.newPlainText(getString(R.string.chat_copy_label), text)

    // 3. Set the clip as the primary clipboard content
    clipboard.setPrimaryClip(clip)

    // Only show Toast for Android 12 (API 32) and below
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        toast(getString(R.string.chat_copy_success))
    }
}

/**
 * Opens the system share sheet to share the given text.
 *
 * @receiver The Context used to start the activity.
 * @param text The string to be shared.
 */
fun Context.shareText(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}


/**
 * Checks if the user has any form of secure "gatekeeping" active.
 * Returns true if biometrics are enrolled OR a lock screen (PIN/Pattern/Pass) is set.
 *
 * @receiver The Context used to access system services and resources.
 * @return True if secure gatekeeping is enabled, false otherwise.
 */
fun Context.isSecurityEnabled(): Boolean {
    return try {
        val biometricManager = BiometricManager.from(this)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // Check for "Strong" Biometrics (Fingerprint, Face, etc.)
        // or Device Credentials (PIN, Pattern, Password)
        val canAuthenticate =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> {
                // Fallback for older APIs or specific edge cases where
                // Keyguard might be set even if BiometricManager is being moody.
                keyguardManager.isDeviceSecure
            }
        }
    } catch (_: Throwable) {
        // Fallback for environments where BiometricManager is not supported (e.g., Layoutlib/Compose Preview)
        try {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.isDeviceSecure ?: false
        } catch (_: Throwable) {
            false
        }
    }
}
