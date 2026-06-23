package off.kys.backtalk.presentation.state

data class SelectionMetrics(
    val selectedMessagesCount: Int = 0,
    val selectedImagesCount: Int = 0,
    val totalSelectedCount: Int = 0,
    val totalDeletableCount: Int = 0
)
