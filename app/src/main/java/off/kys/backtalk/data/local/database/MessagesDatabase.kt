package off.kys.backtalk.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.data.local.dao.ScheduledMessagesDao
import off.kys.backtalk.data.local.database.converter.Converters
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.data.local.entity.ScheduledMessageEntity

/**
 * The Room database for the application, responsible for persisting message data.
 *
 * This database includes the [MessageEntity] and provides access to [MessagesDao].
 */
@Database(
    entities = [MessageEntity::class, ScheduledMessageEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MessagesDatabase: RoomDatabase() {
    /**
     * Returns the Data Access Object (DAO) for the messages table.
     *
     * @return An instance of [MessagesDao].
     */
    abstract fun messagesDao(): MessagesDao

    /**
     * Returns the Data Access Object (DAO) for the scheduled_messages table.
     *
     * @return An instance of [ScheduledMessagesDao].
     */
    abstract fun scheduledMessagesDao(): ScheduledMessagesDao
}