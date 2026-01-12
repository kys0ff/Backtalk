package off.kys.backtalk.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import off.kys.backtalk.R

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

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