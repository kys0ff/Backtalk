package off.kys.backtalk.domain.model

import off.kys.backtalk.data.local.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadGroupingTest {

    private val hour = 3600000L

    private fun message(
        id: Long,
        text: String = "message $id",
        timestamp: Long = id * 1000L,
        repliedToId: MessageId? = null,
        threadId: MessageId? = null
    ) = MessageEntity(
        id = MessageId(id),
        text = text,
        timestamp = timestamp,
        repliedToId = repliedToId,
        threadId = threadId
    )

    @Test
    fun `legacy messages without threadId are grouped by one-hour time gap`() {
        // Legacy messages (threadId = null) use the old time-based grouping
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 1000L),  // Within 1 hour, same thread
            message(3, timestamp = hour + 1000L)  // More than 1 hour, new thread
        )

        val threads = groupMessagesIntoThreads(messages)

        // Newest thread first, so the later root 3 leads.
        assertEquals(listOf(MessageId(3), MessageId(1)), threads.map { it.root.id })
        val thread1 = threads.first { it.root.id == MessageId(1) }
        assertEquals(listOf(MessageId(2)), thread1.replies.map { it.id })
        assertEquals(0, threads.first { it.root.id == MessageId(3) }.replies.size)
    }

    @Test
    fun `new messages with explicit threadId are grouped by threadId`() {
        // New messages (threadId != null) use explicit grouping
        val messages = listOf(
            message(1, timestamp = 0L, threadId = MessageId(1)),
            message(2, timestamp = 10 * hour, threadId = MessageId(1)),  // Hours apart but same thread
            message(3, timestamp = 11 * hour, threadId = MessageId(3))   // Different threadId, different thread
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(2, threads.size)
        val thread1 = threads.first { it.root.id == MessageId(1) }
        assertEquals(listOf(MessageId(2)), thread1.replies.map { it.id })
        assertEquals(MessageId(3), threads.first().root.id)
    }

    @Test
    fun `hybrid grouping works with mixed legacy and new messages`() {
        // Mix of legacy (null threadId) and new (explicit threadId) messages
        val messages = listOf(
            message(1, timestamp = 0L),  // Legacy: thread root
            message(2, timestamp = 1000L),  // Legacy: continues thread 1 (within 1 hour)
            message(3, timestamp = 2 * hour, threadId = MessageId(3)),  // New: explicit thread root
            message(4, timestamp = 2 * hour + 1000L, threadId = MessageId(3)),  // New: explicit continuation
            message(5, timestamp = 10 * hour)  // Legacy: new thread (gap > 1 hour)
        )

        val threads = groupMessagesIntoThreads(messages)

        // Newest root first: 5 (t=10h), then 3 (t=2h), then 1 (t=0).
        assertEquals(listOf(MessageId(5), MessageId(3), MessageId(1)), threads.map { it.root.id })

        // Legacy thread 1
        val thread1 = threads.first { it.root.id == MessageId(1) }
        assertEquals(listOf(MessageId(2)), thread1.replies.map { it.id })

        // New thread 3
        val thread3 = threads.first { it.root.id == MessageId(3) }
        assertEquals(listOf(MessageId(4)), thread3.replies.map { it.id })

        // Legacy thread 5
        assertEquals(0, threads.first { it.root.id == MessageId(5) }.replies.size)
    }

    @Test
    fun `replies with explicit threadId are grouped correctly`() {
        // Replies with explicit threadId belong to that thread
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 1L, repliedToId = MessageId(1), threadId = MessageId(1)),
            message(3, timestamp = 10 * hour, repliedToId = MessageId(1), threadId = MessageId(1))
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(1, threads.size)
        assertEquals(MessageId(1), threads.single().root.id)
        assertEquals(listOf(MessageId(2), MessageId(3)), threads.single().replies.map { it.id })
    }

    @Test
    fun `explicit threadId wins over the message being replied to`() {
        // threadId decides the thread, repliedToId only decides what the reply preview shows. The
        // UI does not currently let the two disagree, but the grouping must not depend on that.
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 1L, repliedToId = MessageId(1), threadId = MessageId(1)),
            message(3, timestamp = 2L, repliedToId = MessageId(2), threadId = MessageId(3))
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(setOf(MessageId(1), MessageId(3)), threads.map { it.root.id }.toSet())
        val firstThread = threads.first { it.root.id == MessageId(1) }
        assertEquals(listOf(MessageId(2)), firstThread.replies.map { it.id })
        // The quoted message is still resolved so the reply preview keeps working.
        val newThread = threads.first { it.root.id == MessageId(3) }
        assertEquals(MessageId(2), newThread.repliedTo?.id)
    }

    @Test
    fun `threads are ordered by the timestamp of their root`() {
        val messages = listOf(
            // Shuffled on purpose: grouping must not depend on the order messages arrive in.
            message(3, timestamp = 3000L, threadId = MessageId(10)),
            message(10, timestamp = 9000L, threadId = MessageId(10)),
            message(2, timestamp = 200L),
            message(11, timestamp = 1000L, threadId = MessageId(10))
        )

        val threads = groupMessagesIntoThreads(messages)

        // Root 10 (t=9000) leads root 2 (t=200) even though message 2 was listed in between.
        assertEquals(listOf(MessageId(10), MessageId(2)), threads.map { it.root.id })
        val thread10 = threads.first { it.root.id == MessageId(10) }
        // Replies are ordered by timestamp, not by the order they were listed in.
        assertEquals(listOf(MessageId(11), MessageId(3)), thread10.replies.map { it.id })
    }

    @Test
    fun `reply whose root is missing is kept as its own thread`() {
        // The root was deleted but its reply survived.
        val messages = listOf(message(2, timestamp = 1L, repliedToId = MessageId(1), threadId = MessageId(1)))

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(1, threads.size)
        assertEquals(MessageId(2), threads.single().root.id)
        assertEquals(0, threads.single().replies.size)
    }

    @Test
    fun `survivors of a deleted root stay in one thread`() {
        // Deleting the first message of a thread must not scatter the rest into one thread each.
        val messages = listOf(
            message(11, timestamp = 1000L, threadId = MessageId(10)),
            message(12, timestamp = 2000L, threadId = MessageId(10)),
            message(13, timestamp = 3000L, threadId = MessageId(10))
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(1, threads.size)
        assertEquals(MessageId(11), threads.single().root.id)
        assertEquals(listOf(MessageId(12), MessageId(13)), threads.single().replies.map { it.id })
    }

    @Test
    fun `a message stored with its own id as thread id starts a thread`() {
        // How "New thread" is persisted: the root carries its own ID, which keeps it out of the
        // legacy time-gap grouping even though it was written seconds after the previous message.
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 1000L, threadId = MessageId(1)),
            message(3, timestamp = 2000L, threadId = MessageId(3)), // "New thread" tapped
            message(4, timestamp = 3000L, threadId = MessageId(3))
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(listOf(MessageId(3), MessageId(1)), threads.map { it.root.id })
        assertEquals(
            listOf(MessageId(4)),
            threads.first { it.root.id == MessageId(3) }.replies.map { it.id }
        )
        assertEquals(
            listOf(MessageId(2)),
            threads.first { it.root.id == MessageId(1) }.replies.map { it.id }
        )
    }

    @Test
    fun `empty input produces no threads`() {
        assertEquals(0, groupMessagesIntoThreads(emptyList()).size)
    }

    @Test
    fun `thread size counts the root plus its replies`() {
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 1L, threadId = MessageId(1)),
            message(3, timestamp = 2L, threadId = MessageId(1))
        )

        assertEquals(3, groupMessagesIntoThreads(messages).single().size)
    }

    @Test
    fun `legacy messages within same hour are in one thread`() {
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 30 * 60 * 1000L),  // 30 minutes later
            message(3, timestamp = 50 * 60 * 1000L)   // 50 minutes after first
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(1, threads.size)
        assertEquals(MessageId(1), threads.single().root.id)
        assertEquals(listOf(MessageId(2), MessageId(3)), threads.single().replies.map { it.id })
    }

    @Test
    fun `legacy and new messages do not interfere with each other`() {
        // Legacy messages use time gaps, new messages use explicit threadId
        val messages = listOf(
            message(1, timestamp = 0L),  // Legacy root
            message(2, timestamp = 1000L),  // Legacy continues 1
            message(3, timestamp = 2000L, threadId = MessageId(3)),  // New root (even though < 1 hour)
            message(4, timestamp = 3000L)  // Legacy continues 1 (still < 1 hour from 2)
        )

        val threads = groupMessagesIntoThreads(messages)

        // Should be 2 threads: legacy thread (1,2,4) and new thread (3)
        assertEquals(2, threads.size)

        val legacyThread = threads.first { it.root.id == MessageId(1) }
        assertEquals(setOf(MessageId(2), MessageId(4)), legacyThread.replies.map { it.id }.toSet())

        val newThread = threads.first { it.root.id == MessageId(3) }
        assertEquals(0, newThread.replies.size)
    }

    @Test
    fun `legacy reply to a root joins that thread however old the root is`() {
        // The rule that used to take priority over the time gap: a message replying to a group's
        // root joins that group even when it was written hours later.
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 5 * hour, repliedToId = MessageId(1))
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(1, threads.size)
        assertEquals(MessageId(1), threads.single().root.id)
        assertEquals(listOf(MessageId(2)), threads.single().replies.map { it.id })
    }

    @Test
    fun `legacy reply to a non-root message falls through to the time gap rule`() {
        // Replying to a message *inside* a group never joined that group: the old algorithm only
        // matched a group's root, so this fell through to the gap rule and started a new thread.
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 1000L),
            message(3, timestamp = 5 * hour, repliedToId = MessageId(2))
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(listOf(MessageId(3), MessageId(1)), threads.map { it.root.id })
        assertEquals(
            listOf(MessageId(2)),
            threads.first { it.root.id == MessageId(1) }.replies.map { it.id }
        )
    }

    @Test
    fun `new message pointing at a legacy thread member joins that thread`() {
        // Replying today to an old conversation must not tear the old thread apart: message 3 points
        // at member 2, and the whole legacy group stays intact with the new reply appended.
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 1000L),
            message(3, timestamp = 20 * hour, repliedToId = MessageId(2), threadId = MessageId(2))
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(1, threads.size)
        assertEquals(MessageId(1), threads.single().root.id)
        assertEquals(
            listOf(MessageId(2), MessageId(3)),
            threads.single().replies.map { it.id }
        )
    }

    @Test
    fun `newest thread comes first so the threads screen keeps its previous order`() {
        // The screen renders the list top-down without reversing it, so the newest thread has to be
        // the first element to stay where users are used to seeing it.
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 3 * hour),
            message(3, timestamp = 6 * hour)
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(listOf(MessageId(3), MessageId(2), MessageId(1)), threads.map { it.root.id })
    }

    @Test
    fun `legacy gap is measured against the newest message of the current group`() {
        // The gap rolls forward: each message is compared to the last one added, so a chain of
        // sub-hour steps stays in one thread even when the first and last are hours apart.
        val messages = listOf(
            message(1, timestamp = 0L),
            message(2, timestamp = 50 * 60 * 1000L),
            message(3, timestamp = 100 * 60 * 1000L)
        )

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(1, threads.size)
        assertEquals(listOf(MessageId(2), MessageId(3)), threads.single().replies.map { it.id })
    }
}
