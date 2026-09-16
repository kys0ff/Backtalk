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
    fun `messages without threadId are each their own thread`() {
        val messages = listOf(message(1), message(2), message(3))

        val threads = groupMessagesIntoThreads(messages)

        assertEquals(3, threads.size)
        assertEquals(listOf(MessageId(1), MessageId(2), MessageId(3)), threads.map { it.root.id })
    }

    @Test
    fun `replies are grouped under their thread root regardless of time gap`() {
        // Two replies written hours apart still belong to the same thread.
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
    fun `explicit threadId starts a new thread even when it replies to another thread`() {
        // A message that quotes a message from thread 1 but was sent with "new thread" enabled.
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
            message(10, timestamp = 9000L),
            message(2, timestamp = 200L),
            message(11, timestamp = 1000L, threadId = MessageId(10))
        )

        val threads = groupMessagesIntoThreads(messages)

        // Root 2 (t=200) precedes root 10 (t=9000) even though message 2 was listed after it.
        assertEquals(listOf(MessageId(2), MessageId(10)), threads.map { it.root.id })
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
}
