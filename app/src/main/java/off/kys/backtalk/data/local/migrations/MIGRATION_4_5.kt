package off.kys.backtalk.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 4 to 5.
 * Adds reminder-related columns to `messages` and creates `scheduled_messages` table.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add columns to messages table
        db.execSQL("ALTER TABLE messages ADD COLUMN isReminder INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE messages ADD COLUMN originalCreationTimestamp INTEGER")
        db.execSQL("ALTER TABLE messages ADD COLUMN scheduledTimestamp INTEGER")

        // Create scheduled_messages table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `scheduled_messages` (
                `id` INTEGER NOT NULL, 
                `text` TEXT NOT NULL, 
                `creationTimestamp` INTEGER NOT NULL, 
                `scheduledTimestamp` INTEGER NOT NULL, 
                `value` INTEGER,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}
