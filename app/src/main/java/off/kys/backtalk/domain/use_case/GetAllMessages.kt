package off.kys.backtalk.domain.use_case

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import off.kys.backtalk.domain.repository.MessagesRepository

class GetAllMessages(
    private val repository: MessagesRepository
) {

    @Composable
    operator fun invoke() = repository.getAllMessages().collectAsState(initial = emptyList())

}