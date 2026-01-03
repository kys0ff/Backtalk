package off.kys.backtalk

import android.content.Context
import androidx.room.Room

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