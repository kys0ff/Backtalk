package off.kys.backtalk.presentation.screen.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import off.kys.backtalk.presentation.state.DayActivity

@Composable
fun ActivityBarChart(
    data: List<DayActivity>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    
    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val spacing = size.width / (data.size * 2 + 1)
        val barWidth = spacing
        
        data.forEachIndexed { index, day ->
            val barHeight = (day.count.toFloat() / maxCount) * size.height
            val x = spacing + (index * (barWidth + spacing))
            val y = size.height - barHeight
            
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun MessageTypePieChart(
    voiceCount: Int,
    textCount: Int,
    voiceLabel: String,
    textLabel: String,
    modifier: Modifier = Modifier
) {
    val voiceColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.secondary
    val total = (voiceCount + textCount).coerceAtLeast(1)
    val voiceAngle = (voiceCount.toFloat() / total) * 360f
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            drawArc(
                color = textColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true
            )
            drawArc(
                color = voiceColor,
                startAngle = -90f,
                sweepAngle = voiceAngle,
                useCenter = true
            )
        }
        
        Spacer(modifier = Modifier.width(32.dp))
        
        Box {
            androidx.compose.foundation.layout.Column {
                ChartLegendItem(color = voiceColor, label = voiceLabel)
                Spacer(modifier = Modifier.height(8.dp))
                ChartLegendItem(color = textColor, label = textLabel)
            }
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
