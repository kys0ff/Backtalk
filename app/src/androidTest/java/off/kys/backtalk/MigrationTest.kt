package off.kys.backtalk

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import off.kys.backtalk.data.local.database.MessagesDatabase
import off.kys.backtalk.data.local.migrations.MIGRATION_6_7
import off.kys.backtalk.data.local.migrations.MIGRATION_8_9
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MessagesDatabase::class.java
    )

    @Test
    fun migrate6To7() {
        helper.createDatabase(TEST_DB, 6).apply {
            // Database has schema version 6. Insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO messages (id, text, timestamp, isPinned) VALUES ('msg1', 'Hello', 1000, 0)")
            close()
        }

        // Re-open the database with version 7 and provide MIGRATION_6_7
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        // Verify that the data is still there and new columns exist
        val cursor = db.query("SELECT * FROM messages WHERE id = 'msg1'")
        assert(cursor.moveToFirst())
        val mediaPathColumnIndex = cursor.getColumnIndex("mediaPath")
        assert(mediaPathColumnIndex != -1)
        val mediaTypeColumnIndex = cursor.getColumnIndex("mediaType")
        assert(mediaTypeColumnIndex != -1)
        cursor.close()
    }

    @Test
    fun migrate8To9() {
        helper.createDatabase(TEST_DB, 8).apply {
            execSQL("INSERT INTO messages (id, text, timestamp, isPinned) VALUES (1000, 'Hello', 1000, 0)")
            close()
        }

        // Re-open the database with version 9 and provide MIGRATION_8_9
        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        // The threadId column must exist on both tables.
        val messageCursor = db.query("SELECT * FROM messages WHERE id = 1000")
        assert(messageCursor.moveToFirst())
        val threadIdColumnIndex = messageCursor.getColumnIndex("threadId")
        assert(threadIdColumnIndex != -1)
        // Pre-existing rows are thread roots, so the column must be left NULL.
        assert(messageCursor.isNull(threadIdColumnIndex))
        messageCursor.close()

        val scheduledCursor = db.query("SELECT * FROM scheduled_messages")
        val scheduledThreadIdColumnIndex = scheduledCursor.getColumnIndex("threadId")
        scheduledCursor.close()
        assert(scheduledThreadIdColumnIndex != -1)
    }
}
