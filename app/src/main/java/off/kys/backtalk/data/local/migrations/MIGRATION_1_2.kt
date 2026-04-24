package off.kys.backtalk.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 1 to 2.
 * Adds the `editedAt` column to the `messages` table.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN editedAt INTEGER")
    }
}