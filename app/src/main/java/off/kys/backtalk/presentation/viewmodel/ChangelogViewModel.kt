package off.kys.backtalk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.repository.ChangelogRepository
import off.kys.backtalk.presentation.state.changelog.ChangelogUiState

class ChangelogViewModel(
    private val repository: ChangelogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChangelogUiState())
    val state = _state.asStateFlow()

    init {
        loadChangelog()
    }

    private fun loadChangelog() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true) }
            val entries = repository.getChangelogEntries()
            _state.update {
                it.copy(
                    entries = entries,
                    isLoading = false
                )
            }
        }
    }
}
