package off.kys.backtalk.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.ThreadsUiEvent
import off.kys.backtalk.presentation.state.ThreadsUiState

class ThreadsViewModel(
    private val useCases: MessagesUseCases
) : ViewModel() {

    private val _uiState = mutableStateOf(ThreadsUiState())
    val uiState: State<ThreadsUiState> = _uiState

    private var allMessages: List<MessageEntity> = emptyList()

    init {
        loadMessages()
    }

    fun onEvent(event: ThreadsUiEvent) = when (event) {
        is ThreadsUiEvent.LoadThreads -> loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages().collectLatest { messages ->
                allMessages = messages
                val grouped = groupMessages(
                    messages
                )
                _uiState.value = _uiState.value.copy(threads = grouped)
            }
        }
    }

    /**
     * Creates a focused thread starting from the given message, including all its descendants.
     */
    fun getSubThread(rootMessage: MessageEntity): Thread {
        val messageSource = allMessages.ifEmpty {
            uiState.value.threads.flatMap { listOf(it.root) + it.replies }
        }

        val descendants = mutableListOf<MessageEntity>()
        val queue = mutableListOf(rootMessage.id)
        val visited = mutableSetOf(rootMessage.id)

        while (queue.isNotEmpty()) {
            val parentId = queue.removeAt(0)
            val children = messageSource.filter { it.repliedToId == parentId }

            for (child in children) {
                if (child.id !in visited) {
                    descendants.add(child)
                    visited.add(child.id)
                    queue.add(child.id)
                }
            }
        }

        return Thread(
            root = rootMessage,
            replies = descendants.sortedBy { it.timestamp }
        )
    }

    private fun groupMessages(messages: List<MessageEntity>): List<Thread> {
        if (messages.isEmpty()) return emptyList()

        val sorted = messages.sortedBy { it.timestamp }
        val messageMap = sorted.associateBy { it.id }
        val groups = mutableListOf<MutableList<MessageEntity>>()

        sorted.forEach { message ->
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
                if (lastGroup != null) {
                    val lastMessage = lastGroup.last()
                    if (message.timestamp - lastMessage.timestamp < Constants.TIME_GAP_FOR_HEADER) {
                        lastGroup.add(message)
                        foundGroup = true
                    }
                }
            }

            if (!foundGroup) {
                groups.add(mutableListOf(message))
            }
        }

        return groups.map { group ->
            val root = group.first()
            val repliedTo = root.repliedToId?.let { messageMap[it] }
            Thread(root = root, replies = group.drop(1), repliedTo = repliedTo)
        }.reversed()
    }
}
