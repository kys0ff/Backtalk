package off.kys.backtalk.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 2 to 3.
 * Adds the `editedText` column to the `messages` table to support storing the modified content of a message.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN editedText TEXT")
    }
}
