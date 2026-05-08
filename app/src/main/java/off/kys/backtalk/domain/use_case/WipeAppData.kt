package off.kys.backtalk.domain.use_case

import android.content.Context
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.dao.MessagesDao
import java.io.File

/**
 * Use case to wipe all application data, including database, preferences, and files.
 */
class WipeAppData(
    private val context: Context,
    private val messagesDao: MessagesDao,
    private val preferences: BacktalkPreferences
) {
    /**
     * Executes the wipe operation.
     */
    suspend operator fun invoke() {
        // Clear Database
        messagesDao.deleteAllMessages()

        // Clear Preferences
        preferences.clearAll()

        // Clear Files (Voice Messages)
        val voiceMessagesDir = File(context.filesDir, "voice_messages")
        if (voiceMessagesDir.exists()) {
            voiceMessagesDir.deleteRecursively()
        }

        // Clear Cache
        context.cacheDir.deleteRecursively()
    }
}
