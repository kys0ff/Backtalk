package off.kys.backtalk.presentation.screen.statistics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.state.StatisticsUiState

@Composable
fun StatisticsContent(
    state: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = stringResource(R.string.statistics_total_messages),
                value = state.totalMessages.toString(),
                icon = painterResource(R.drawable.round_chat_bubble_outline_24),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.statistics_current_streak),
                value = state.currentStreak.toString(),
                icon = painterResource(R.drawable.round_mode_heat_24),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = stringResource(R.string.statistics_scheduled),
                value = state.scheduledMessagesCount.toString(),
                icon = painterResource(R.drawable.round_access_alarm_24),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.statistics_avg_length),
                value = state.avgMessageLength.toString(),
                icon = painterResource(R.drawable.round_abc_24),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val voiceTime = formatDuration(state.totalVoiceDurationMs)
            StatCard(
                label = stringResource(R.string.statistics_voice_duration),
                value = voiceTime,
                icon = painterResource(R.drawable.round_keyboard_voice_24),
                containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.weight(1f)
            )

            val imageCount = state.imageCount.toString()
            StatCard(
                label = stringResource(R.string.statistics_images),
                value = imageCount,
                icon = painterResource(R.drawable.round_image_24),
                containerColor = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.weight(1f)
            )
        }

        SectionTitle(stringResource(R.string.statistics_activity_last_7_days))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                ActivityBarChart(data = state.activityLast7Days)
            }
        }

        SectionTitle(stringResource(R.string.statistics_message_types))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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

        SectionTitle(stringResource(R.string.statistics_top_threads))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.topThreads.forEach { thread ->
                ThreadStatItem(thread)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
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