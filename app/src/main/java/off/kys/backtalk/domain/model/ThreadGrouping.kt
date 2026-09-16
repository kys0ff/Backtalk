package off.kys.backtalk.domain.model

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.data.local.entity.resolvedThreadId

/**
 * Groups [MessageEntity] records into [Thread]s.
 *
 * Thread membership is stored explicitly in [MessageEntity.threadId]: every message carries the ID of
 * the root message of the thread it belongs to, and a `null` [MessageEntity.threadId] means the
 * message is its own root (see `MessageEntity.resolvedThreadId`). A message is therefore placed in
 * the thread whose root has the same ID, and nothing else — the grouping no longer depends on the
 * order in which messages were written nor on how much time passed between them.
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

    val roots = sorted.filter { it.resolvedThreadId == it.id }
    val rootIds = roots.mapTo(mutableSetOf()) { it.id }
    val orphans = sorted.filter { it.resolvedThreadId != it.id && it.resolvedThreadId !in rootIds }

    val repliesByRoot = sorted
        .filter { it.resolvedThreadId != it.id && it.resolvedThreadId in rootIds }
        .groupBy { it.resolvedThreadId }

    val groupedRoots = roots.map { root ->
        Thread(
            root = root,
            replies = repliesByRoot[root.id].orEmpty(),
            repliedTo = root.repliedToId?.let { messageMap[it] }
        )
    }

    val groupedOrphans = orphans.map { orphan ->
        Thread(
            root = orphan,
            replies = emptyList(),
            repliedTo = orphan.repliedToId?.let { messageMap[it] }
        )
    }

    return (groupedRoots + groupedOrphans).sortedBy { it.root.timestamp }
}
