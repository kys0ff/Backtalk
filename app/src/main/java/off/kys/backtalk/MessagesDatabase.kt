package off.kys.backtalk

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class MessagesDatabase: RoomDatabase() {

    abstract fun messagesDao(): MessagesDao

}