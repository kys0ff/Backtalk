package off.kys.backtalk.presentation.screen.statistics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.state.statistics.StatisticsUiState

@Composable
fun StatisticsContent(
    state: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 32.dp,
            top = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(stringResource(R.string.statistics_activity_last_7_days))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.statistics_current_streak),
                    value = state.currentStreak.toString(),
                    icon = painterResource(R.drawable.round_mode_heat_24),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f),
                    subValue = "days"
                )
                StatCard(
                    label = stringResource(R.string.statistics_best_streak),
                    value = state.bestStreak.toString(),
                    icon = painterResource(R.drawable.round_star_24),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f),
                    subValue = "days"
                )
            }
        }

        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder(enabled = true).copy(width = 1.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    ActivityBarChart(data = state.activityLast7Days)
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.statistics_total_messages))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.statistics_total_messages),
                    value = state.totalMessages.toString(),
                    icon = painterResource(R.drawable.round_chat_bubble_outline_24),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.statistics_scheduled),
                    value = state.scheduledMessagesCount.toString(),
                    icon = painterResource(R.drawable.round_access_alarm_24),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.statistics_avg_length),
                    value = state.avgMessageLength.toString(),
                    icon = painterResource(R.drawable.round_abc_24),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f),
                    subValue = "chars"
                )
                StatCard(
                    label = stringResource(R.string.statistics_images),
                    value = state.imageCount.toString(),
                    icon = painterResource(R.drawable.round_image_24),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val voiceTime = formatDuration(state.totalVoiceDurationMs)
            StatCard(
                label = stringResource(R.string.statistics_voice_duration),
                value = voiceTime,
                icon = painterResource(R.drawable.round_keyboard_voice_24),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SectionTitle(stringResource(R.string.statistics_app_usage_heatmap))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder(enabled = true).copy(width = 1.dp)
            ) {
                AppUsageHeatmap(
                    data = state.heatmapData,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.statistics_message_types))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder(enabled = true).copy(width = 1.dp)
            ) {
                MessageTypePieChart(
                    slices = listOf(
                        PieSlice(
                            count = state.textMessagesCount,
                            label = stringResource(R.string.statistics_text),
                            color = MaterialTheme.colorScheme.primary
                        ),
                        PieSlice(
                            count = state.voiceMessagesCount,
                            label = stringResource(R.string.statistics_voice),
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        PieSlice(
                            count = state.imageCount,
                            label = stringResource(R.string.statistics_images),
                            color = MaterialTheme.colorScheme.tertiary
                        ),
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.statistics_top_threads))
        }

        items(state.topThreads) { thread ->
            ThreadStatItem(thread)
        }
    }
}

@Composable
private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        stringResource(R.string.statistics_duration_minutes_seconds, minutes, remainingSeconds)
    } else {
        stringResource(R.string.statistics_duration_seconds, seconds)
    }
}