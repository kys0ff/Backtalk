package off.kys.backtalk.domain.use_case

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.domain.repository.SyncRepository
import off.kys.backtalk.util.ignoreResult
import java.io.File

/**
 * Use case to wipe all application data, including database, preferences, and files.
 */
class WipeAppData(
    private val context: Context,
    private val messagesDao: MessagesDao,
    private val preferences: BacktalkPreferences,
    private val syncRepository: SyncRepository
) {
    /**
     * Executes the wipe operation.
     */
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        // Notify and disconnect all paired devices
        syncRepository.disconnectAll()

        // Clear Database
        messagesDao.deleteAllMessages()

        // Clear Preferences
        preferences.clearAll()

        // Clear Files (Media)
        val mediaDir = File(context.filesDir, "media")
        if (mediaDir.exists()) {
            mediaDir.deleteRecursively()
        }

        val voiceMessagesDir = File(context.filesDir, "voice_messages")
        if (voiceMessagesDir.exists()) {
            voiceMessagesDir.deleteRecursively()
        }

        // Clear Cache
        ignoreResult { context.cacheDir.deleteRecursively() }
    }
}
