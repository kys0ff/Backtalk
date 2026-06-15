package off.kys.backtalk.presentation.state

data class SelectionMetrics(
    val selectedMessagesCount: Int,
    val selectedImagesCount: Int,
    val totalSelectedCount: Int,
    val totalDeletableCount: Int
)