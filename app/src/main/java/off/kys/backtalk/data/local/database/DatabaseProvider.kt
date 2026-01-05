package off.kys.backtalk.data.local.database

import android.content.Context
import androidx.room.Room

// TODO: Delete
class DatabaseProvider(
    private val context: Context
) {
    @Volatile
    private var databaseInstance: MessagesDatabase? = null

    fun getDatabase(): MessagesDatabase = databaseInstance ?: synchronized(this) {
        val instance = Room.databaseBuilder(
            context.applicationContext,
            MessagesDatabase::class.java,
            "msgs_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
        databaseInstance = instance
        instance
    }
}