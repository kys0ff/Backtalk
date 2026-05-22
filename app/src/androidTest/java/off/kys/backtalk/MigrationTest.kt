package off.kys.backtalk

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import off.kys.backtalk.data.local.database.MessagesDatabase
import off.kys.backtalk.data.local.migrations.MIGRATION_6_7
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
}
