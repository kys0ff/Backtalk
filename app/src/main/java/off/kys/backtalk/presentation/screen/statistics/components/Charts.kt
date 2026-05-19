package off.kys.backtalk.presentation.screen.statistics.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import off.kys.backtalk.presentation.state.DayActivity

@Composable
fun ActivityBarChart(
    data: List<DayActivity>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
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
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { day ->
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = day.dayName.take(1),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
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
            Canvas(modifier = Modifier.size(150.dp)) {
                val strokeWidth = 16.dp.toPx()
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
        }

        Spacer(modifier = Modifier.width(32.dp))

        Column {
            slices.fastForEach { slice ->
                val percentage = (slice.count.toFloat() / total) * 100f
                ChartLegendItem(
                    color = slice.color,
                    label = "${slice.label}: ${percentage.toInt()}%"
                )
                Spacer(modifier = Modifier.height(8.dp))
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
private fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}
