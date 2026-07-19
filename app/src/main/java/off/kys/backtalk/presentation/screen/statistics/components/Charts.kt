package off.kys.backtalk.presentation.screen.statistics.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import off.kys.backtalk.presentation.state.statistics.DayActivity
import off.kys.backtalk.presentation.state.statistics.HeatmapDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import androidx.compose.ui.graphics.Brush

@Composable
fun ActivityBarChart(
    data: List<DayActivity>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val barColorSecondary = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val labelColor = MaterialTheme.colorScheme.onSurface
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    // A simple trigger to start the animation when the Composable enters the composition
    val animationTriggered = remember { mutableStateOf(false) }
    val animateProgress by animateFloatAsState(
        targetValue = if (animationTriggered.value) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "BarChartAnimation"
    )

    LaunchedEffect(Unit) {
        animationTriggered.value = true
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val barCount = data.size
            val canvasWidth = size.width
            val canvasHeight = size.height

            val barWidth = canvasWidth / (barCount * 2f)

            data.forEachIndexed { index, day ->
                val percentage = day.count.toFloat() / maxCount
                val barHeight = percentage * canvasHeight * animateProgress

                val x = barWidth / 2 + (index * (barWidth + barWidth))
                val y = canvasHeight - barHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barColor, barColorSecondary)
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { day ->
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = day.dayName.take(1),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = labelColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AppUsageHeatmap(
    data: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    cellSize: Dp = 10.dp,
    cellSpacing: Dp = 4.dp
) {
    if (data.isEmpty()) return

    // Memoize the data processing so we don't choke the UI thread on recomposition
    val weeks = remember(data) { data.chunked(7) }
    val maxCount = remember(data) { data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1 }

    val colorScheme = MaterialTheme.colorScheme
    val baseColor = colorScheme.primary
    val emptyColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)

    val locale = LocalLocale.current.platformLocale
    val dayLabels = remember(locale) {
        listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        ).map { it.getDisplayName(TextStyle.NARROW, locale) }
    }
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Day labels (Static, doesn't scroll)
        Column(
            modifier = Modifier.padding(top = 24.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            dayLabels.forEachIndexed { index, label ->
                if (index % 2 == 1) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.height(cellSize)
                    )
                } else {
                    Spacer(modifier = Modifier.height(cellSize))
                }
            }
        }

        // Heatmap Grid (Scrollable)
        Column(modifier = Modifier.horizontalScroll(scrollState)) {
            // Month labels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                weeks.forEachIndexed { index, week ->
                    val firstDay = week.firstOrNull()?.date
                    // A slightly more robust check for month boundaries
                    val isNewMonth = firstDay != null && (index == 0 || firstDay.dayOfMonth <= 7)

                    if (isNewMonth) {
                        Text(
                            text = firstDay.month.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            // Dynamically calculate padding based on cell constraints
                            modifier = Modifier.padding(start = ((cellSize + cellSpacing) * index))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(cellSpacing))

            Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
                weeks.forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
                        week.forEach { day ->
                            val color = if (day.count == 0) {
                                emptyColor
                            } else {
                                val alpha = (day.count.toFloat() / maxCount).coerceIn(0.2f, 1f)
                                baseColor.copy(alpha = alpha)
                            }

                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .background(color, RoundedCornerShape(3.dp))
                                    // Actually tell the OS what this box is for
                                    .semantics {
                                        contentDescription = "Date: ${day.date}, Count: ${day.count}"
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppUsageHeatmapPreview() {
    val data = List(112) { i ->
        HeatmapDay(
            date = LocalDate.now().minusDays(111L - i),
            count = (0..10).random()
        )
    }
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AppUsageHeatmap(data = data)
        }
    }
}

@Composable
fun MessageTypePieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.count }.coerceAtLeast(1)
    
    val animationTriggered = remember { mutableStateOf(false) }
    val animateProgress by animateFloatAsState(
        targetValue = if (animationTriggered.value) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "PieChartAnimation"
    )

    LaunchedEffect(Unit) {
        animationTriggered.value = true
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 14.dp.toPx()
                var startAngle = -90f

                slices.forEach { slice ->
                    val sweepAngle = (slice.count.toFloat() / total) * 360f * animateProgress
                    if (sweepAngle > 0) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += (slice.count.toFloat() / total) * 360f
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.fastForEach { slice ->
                val percentage = (slice.count.toFloat() / total) * 100f
                ChartLegendItem(
                    color = slice.color,
                    label = slice.label,
                    value = "${percentage.toInt()}%"
                )
            }
        }
    }
}

data class PieSlice(
    val count: Int,
    val label: String,
    val color: Color
)

@Composable
private fun ChartLegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
