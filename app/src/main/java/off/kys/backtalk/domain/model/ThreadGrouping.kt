package off.kys.backtalk.domain.model

import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * Groups [MessageEntity] records into [Thread]s, honouring how each message was written.
 *
 * - **Messages written before the `threadId` column existed** (`threadId` is null) keep the exact
 *   grouping they already had: the reply-to-root and one-hour-gap rules described in
 *   [groupLegacyMessages]. Upgrading therefore leaves a user's existing threads untouched.
 * - **Messages written after it** carry their thread explicitly, so their grouping no longer depends
 *   on the order they were written in nor on how much time passed between them.
 *
 * The two live side by side: a new message may point at any member of an existing legacy thread, in
 * which case it joins that thread rather than splitting it.
 *
 * Messages that reference a root which is not present in [messages] are kept as their own thread so
 * that they are never silently dropped. This can happen when a thread root was deleted while its
 * replies were not.
 *
 * @param messages The messages to group, in any order.
 * @return One [Thread] per root, newest root first so the most recent thread stays at the top of the
 * list, as it was before. Replies inside a thread are ordered by timestamp, oldest first.
 */
fun groupMessagesIntoThreads(messages: List<MessageEntity>): List<Thread> {
    if (messages.isEmpty()) return emptyList()

    val sorted = messages.sortedBy { it.timestamp }
    val messageMap = sorted.associateBy { it.id }

    // Separate legacy messages (threadId == null) from new messages (threadId != null).
    val legacyMessages = sorted.filter { it.threadId == null }
    val newMessages = sorted.filter { it.threadId != null }

    // Legacy messages keep the exact grouping they had before `threadId` existed.
    val legacyGroups = groupLegacyMessages(legacyMessages)

    // Every legacy message maps to the root of the group it ended up in, so a new message pointing
    // at any member of an existing thread joins that thread instead of splitting it apart.
    val legacyRootByMemberId = mutableMapOf<MessageId, MessageId>()
    legacyGroups.forEach { group ->
        val rootId = group.first().id
        group.forEach { legacyRootByMemberId[it.id] = rootId }
    }

    val newRoots = newMessages.filter { it.threadId == it.id }
    val newRootIds = newRoots.mapTo(mutableSetOf()) { it.id }

    // Resolve every non-root new message to the root of the thread that hosts it. A message whose
    // thread root is gone is kept as its own thread rather than being dropped.
    val newRepliesByRoot = mutableMapOf<MessageId, MutableList<MessageEntity>>()
    val orphans = mutableListOf<MessageEntity>()
    newMessages.filter { it.threadId != it.id }.forEach { message ->
        val threadId = message.threadId ?: return@forEach
        val hostRootId = when {
            threadId in newRootIds -> threadId
            else -> legacyRootByMemberId[threadId]
        }
        if (hostRootId == null) orphans.add(message)
        else newRepliesByRoot.getOrPut(hostRootId) { mutableListOf() }.add(message)
    }

    val legacyThreads = legacyGroups.map { group ->
        val root = group.first()
        val replies = (group.drop(1) + newRepliesByRoot[root.id].orEmpty()).sortedBy { it.timestamp }
        Thread(
            root = root,
            replies = replies,
            repliedTo = root.repliedToId?.let { messageMap[it] }
        )
    }

    val newRootThreads = newRoots.map { root ->
        Thread(
            root = root,
            replies = newRepliesByRoot[root.id].orEmpty().sortedBy { it.timestamp },
            repliedTo = root.repliedToId?.let { messageMap[it] }
        )
    }

    // Survivors of a deleted root stay together under the oldest of them, so removing the first
    // message of a thread does not scatter the rest into one thread per message.
    val orphanThreads = orphans.groupBy { it.threadId }.map { (_, survivors) ->
        val root = survivors.first()
        Thread(
            root = root,
            replies = survivors.drop(1),
            repliedTo = root.repliedToId?.let { messageMap[it] }
        )
    }

    // Newest thread first: the list used to be built oldest-first and reversed at the end, and the
    // threads screen renders it top-down without reversing, so this keeps the order users had.
    return (legacyThreads + newRootThreads + orphanThreads).sortedByDescending { it.root.timestamp }
}

/**
 * Groups messages that predate the `threadId` column, reproducing the grouping they already had.
 *
 * This is the algorithm that used to live in `ThreadsViewModel.groupMessages()`, kept verbatim so
 * that upgrading does not reshuffle threads a user has already seen:
 *
 * 1. A message replying to the **root** of an existing group joins that group, however long ago
 *    that root was written.
 * 2. Otherwise the message continues the most recently started group as long as it was written less
 *    than [Constants.TIME_GAP_FOR_HEADER] after that group's newest message.
 * 3. Otherwise it starts a group of its own.
 *
 * @param legacyMessages Messages with a null `threadId`, ordered by timestamp.
 * @return The groups in the order they were started; each group holds its root first.
 */
private fun groupLegacyMessages(legacyMessages: List<MessageEntity>): List<List<MessageEntity>> {
    if (legacyMessages.isEmpty()) return emptyList()

    val groups = mutableListOf<MutableList<MessageEntity>>()

    legacyMessages.forEach { message ->
        var foundGroup = false

        if (message.repliedToId != null) {
            for (group in groups) {
                if (group.first().id == message.repliedToId) {
                    group.add(message)
                    foundGroup = true
                    break
                }
            }
        }

        if (!foundGroup) {
            val lastGroup = groups.lastOrNull()
            if (lastGroup != null &&
                message.timestamp - lastGroup.last().timestamp < Constants.TIME_GAP_FOR_HEADER
            ) {
                lastGroup.add(message)
                foundGroup = true
            }
        }

        if (!foundGroup) groups.add(mutableListOf(message))
    }

    return groups
}
