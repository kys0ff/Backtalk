package off.kys.backtalk.domain.model

import kotlinx.serialization.Serializable
import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * Represents the structured data for a backup, containing messages and preferences.
 *
 * @property version The version of the backup format.
 * @property messages List of all messages to be backed up.
 * @property preferences Map of key-value pairs representing user settings.
 */
@Serializable
data class BackupData(
    val version: Int = 1,
    val messages: List<MessageEntity>,
    val preferences: Map<String, String>
)
