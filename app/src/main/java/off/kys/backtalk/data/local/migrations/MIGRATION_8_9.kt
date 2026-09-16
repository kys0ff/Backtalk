package off.kys.backtalk.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 8 to 9.
 * Adds the `threadId` column to the `messages` and `scheduled_messages` tables.
 *
 * Existing rows are left as `NULL`, which marks the message as a thread root. That matches how the
 * previous time-gap based grouping treated every message that was stored before this change, so
 * upgrading does not reshuffle a user's existing threads.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN threadId INTEGER")
        db.execSQL("ALTER TABLE scheduled_messages ADD COLUMN threadId INTEGER")
    }
}
