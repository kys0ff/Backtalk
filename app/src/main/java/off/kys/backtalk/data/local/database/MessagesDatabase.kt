package off.kys.backtalk.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class MessagesDatabase: RoomDatabase() {

    abstract fun messagesDao(): MessagesDao

}