package off.kys.backtalk.domain.model

import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.data.local.entity.resolvedThreadId

/**
 * Groups [MessageEntity] records into [Thread]s using a hybrid approach:
 *
 * - **New messages** (with an explicit [MessageEntity.threadId]): grouped by that ID.
 * - **Legacy messages** (where [MessageEntity.threadId] is null): grouped by the one-hour time gap
 *   heuristic, preserving the thread structure users had before the explicit threadId was introduced.
 *
 * This hybrid strategy maintains backward compatibility while enabling explicit thread control for
 * new messages. Legacy messages are first grouped by time gaps, then each time-gap-based group is
 * treated as a thread. New messages reference their thread explicitly.
 *
 * Messages that reference a root which is not present in [messages] are kept as their own thread so
 * that they are never silently dropped. This can happen when a thread root was deleted while its
 * replies were not.
 *
 * @param messages The messages to group, in any order.
 * @return One [Thread] per root, ordered by the timestamp of their root, oldest first. Replies are
 * ordered by timestamp as well.
 */
fun groupMessagesIntoThreads(messages: List<MessageEntity>): List<Thread> {
    if (messages.isEmpty()) return emptyList()

    val sorted = messages.sortedBy { it.timestamp }
    val messageMap = sorted.associateBy { it.id }

    // Separate legacy messages (threadId == null) from new messages (threadId != null)
    val legacyMessages = sorted.filter { it.threadId == null }
    val newMessages = sorted.filter { it.threadId != null }

    // Find which legacy messages are referenced by new messages
    // These should be treated as explicit roots, not grouped by time
    val legacyRootsReferencedByNewMessages = newMessages
        .mapNotNull { it.threadId }
        .filter { it in legacyMessages.map { msg -> msg.id } }
        .toSet()

    // Split legacy messages into two groups:
    // 1. Those referenced by new messages (treat as explicit roots)
    // 2. Pure legacy messages (group by time gap)
    val explicitLegacyRoots = legacyMessages.filter { it.id in legacyRootsReferencedByNewMessages }
    val pureLegacyMessages = legacyMessages.filter { it.id !in legacyRootsReferencedByNewMessages }

    // Group pure legacy messages using the one-hour time gap heuristic
    val pureLegacyThreads = groupLegacyMessagesByTimeGap(pureLegacyMessages, messageMap)

    // Group new messages by their explicit threadId
    val newRoots = newMessages.filter { it.resolvedThreadId == it.id }
    val allRootIds = (legacyMessages.map { it.id } + newRoots.map { it.id }).toSet()

    val repliesByRoot = newMessages
        .filter { it.resolvedThreadId != it.id && it.resolvedThreadId in allRootIds }
        .groupBy { it.resolvedThreadId }

    // Create threads for explicit legacy roots (with new replies)
    val explicitLegacyThreads = explicitLegacyRoots.map { root ->
        Thread(
            root = root,
            replies = repliesByRoot[root.id].orEmpty(),
            repliedTo = root.repliedToId?.let { messageMap[it] }
        )
    }

    // Create threads for new roots
    val newRootThreads = newRoots.map { root ->
        Thread(
            root = root,
            replies = repliesByRoot[root.id].orEmpty(),
            repliedTo = root.repliedToId?.let { messageMap[it] }
        )
    }

    // Handle orphans (messages whose root is missing)
    val orphans = newMessages.filter { it.resolvedThreadId != it.id && it.resolvedThreadId !in allRootIds }
    val orphanThreads = orphans.map { orphan ->
        Thread(
            root = orphan,
            replies = emptyList(),
            repliedTo = orphan.repliedToId?.let { messageMap[it] }
        )
    }

    return (pureLegacyThreads + explicitLegacyThreads + newRootThreads + orphanThreads)
        .sortedBy { it.root.timestamp }
}

/**
 * Groups legacy messages (with null threadId) using the one-hour time gap heuristic.
 *
 * This preserves the thread structure that existed before explicit threadId was introduced.
 * Messages separated by more than [Constants.TIME_GAP_FOR_HEADER] are considered separate threads.
 */
private fun groupLegacyMessagesByTimeGap(
    legacyMessages: List<MessageEntity>,
    messageMap: Map<MessageId, MessageEntity>
): List<Thread> {
    if (legacyMessages.isEmpty()) return emptyList()

    val threads = mutableListOf<Thread>()
    var currentThread = mutableListOf<MessageEntity>()

    legacyMessages.forEachIndexed { index, message ->
        if (index == 0) {
            currentThread.add(message)
        } else {
            val previousMessage = legacyMessages[index - 1]
            val timeDiff = message.timestamp - previousMessage.timestamp

            if (timeDiff >= Constants.TIME_GAP_FOR_HEADER) {
                // Time gap exceeded, finalize current thread and start a new one
                if (currentThread.isNotEmpty()) {
                    threads.add(createThreadFromGroup(currentThread, messageMap))
                    currentThread = mutableListOf()
                }
            }
            currentThread.add(message)
        }
    }

    // Add the last thread
    if (currentThread.isNotEmpty()) {
        threads.add(createThreadFromGroup(currentThread, messageMap))
    }

    return threads
}

/**
 * Creates a [Thread] from a group of messages, treating the first as root and the rest as replies.
 */
private fun createThreadFromGroup(
    group: List<MessageEntity>,
    messageMap: Map<MessageId, MessageEntity>
): Thread {
    val root = group.first()
    val replies = group.drop(1)
    return Thread(
        root = root,
        replies = replies,
        repliedTo = root.repliedToId?.let { messageMap[it] }
    )
}
