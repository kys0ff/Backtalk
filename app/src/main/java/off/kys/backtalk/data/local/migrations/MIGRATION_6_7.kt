package off.kys.backtalk.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 6 to version 7.
 * Adds the `mediaPath` and `mediaType` columns to the `messages` and `scheduled_messages` tables.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN mediaPath TEXT")
        db.execSQL("ALTER TABLE messages ADD COLUMN mediaType TEXT")
        db.execSQL("ALTER TABLE scheduled_messages ADD COLUMN mediaPath TEXT")
        db.execSQL("ALTER TABLE scheduled_messages ADD COLUMN mediaType TEXT")
    }
}
