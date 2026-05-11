package off.kys.backtalk.presentation.screen.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.R
import off.kys.backtalk.presentation.components.HintTooltip
import off.kys.backtalk.presentation.screen.statistics.components.ActivityBarChart
import off.kys.backtalk.presentation.screen.statistics.components.MessageTypePieChart
import off.kys.backtalk.presentation.state.StatisticsUiState
import off.kys.backtalk.presentation.state.ThreadStat
import off.kys.backtalk.presentation.viewmodel.StatisticsViewModel
import org.koin.compose.viewmodel.koinViewModel

class StatisticsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<StatisticsViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.statistics_title)) },
                    navigationIcon = {
                        HintTooltip(stringResource(R.string.common_back)) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_arrow_back_24),
                                    contentDescription = stringResource(R.string.common_back)
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                StatisticsContent(
                    state = state,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    state: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = stringResource(R.string.statistics_total_messages),
                value = state.totalMessages.toString(),
                icon = painterResource(R.drawable.round_chat_bubble_outline_24),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.statistics_scheduled),
                value = state.scheduledMessagesCount.toString(),
                icon = painterResource(R.drawable.round_access_alarm_24),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val voiceTime = formatDuration(state.totalVoiceDurationMs)
            StatCard(
                label = stringResource(R.string.statistics_voice_duration),
                value = voiceTime,
                icon = painterResource(R.drawable.round_keyboard_voice_24),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.statistics_avg_length),
                value = state.avgMessageLength.toString(),
                icon = painterResource(R.drawable.round_code_24),
                modifier = Modifier.weight(1f)
            )
        }

        // Activity Chart
        SectionTitle(stringResource(R.string.statistics_activity_last_7_days))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                ActivityBarChart(data = state.activityLast7Days)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.activityLast7Days.forEach {
                        Text(
                            text = it.dayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Message Types Pie Chart
        SectionTitle(stringResource(R.string.statistics_message_types))
        Card(modifier = Modifier.fillMaxWidth()) {
            MessageTypePieChart(
                voiceCount = state.voiceMessagesCount,
                textCount = state.textMessagesCount,
                voiceLabel = stringResource(R.string.statistics_voice),
                textLabel = stringResource(R.string.statistics_text),
                modifier = Modifier.padding(16.dp)
            )
        }

        // Top Threads
        SectionTitle(stringResource(R.string.statistics_top_threads))
        state.topThreads.forEach { thread ->
            ThreadStatItem(thread)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: Painter,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThreadStatItem(stat: ThreadStat) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stat.threadTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.statistics_messages_count, stat.messageCount),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { stat.ratio },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${seconds}s"
    }
}
