package off.kys.backtalk.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 7 to 8.
 * Adds the `mediaPaths` column to the `messages` and `scheduled_messages` tables.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN mediaPaths TEXT")
        db.execSQL("ALTER TABLE scheduled_messages ADD COLUMN mediaPaths TEXT")
    }
}
