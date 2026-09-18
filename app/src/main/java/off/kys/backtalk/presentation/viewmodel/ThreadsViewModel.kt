package off.kys.backtalk.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.domain.model.groupMessagesIntoThreads
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.state.threads.ThreadsUiState

class ThreadsViewModel(
    private val useCases: MessagesUseCases
) : ViewModel() {

    private val _uiState = mutableStateOf(ThreadsUiState())
    val uiState: State<ThreadsUiState> = _uiState

    private var allMessages: List<MessageEntity> = emptyList()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages().collectLatest { messages ->
                allMessages = messages
                val grouped = withContext(Dispatchers.Default) {
                    groupMessagesIntoThreads(messages).toPersistentList()
                }
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
}
