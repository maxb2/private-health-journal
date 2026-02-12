package com.privatehealthjournal.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.compose.component.textComponent
import androidx.compose.ui.unit.sp
import android.graphics.Typeface
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.privatehealthjournal.ui.utils.formatDateForAxis

@Composable
fun EmptyChartState(message: String = "No data available") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun WeightChart(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        EmptyChartState("No weight data to display")
        return
    }

    val sortedEntries = remember(entries) { entries.sortedBy { it.timestamp } }
    val chartColor = MaterialTheme.colorScheme.primary
    val unit = sortedEntries.lastOrNull()?.unit ?: WeightUnit.LB

    val minTimestamp = remember(sortedEntries) { sortedEntries.minOf { it.timestamp } }
    val msPerDay = 24 * 60 * 60 * 1000L

    val chartEntryModelProducer = remember(sortedEntries) {
        ChartEntryModelProducer(
            sortedEntries.map { entry ->
                val dayOffset = ((entry.timestamp - minTimestamp) / msPerDay).toFloat()
                entryOf(dayOffset, entry.weight.toFloat())
            }
        )
    }

    val lineSpec = remember(chartColor) {
        listOf(
            LineChart.LineSpec(
                lineColor = chartColor.toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            chartColor.copy(alpha = 0.4f),
                            chartColor.copy(alpha = 0f)
                        )
                    )
                )
            )
        )
    }

    val bottomAxisFormatter = remember(sortedEntries, minTimestamp) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val timestamp = minTimestamp + (value * msPerDay).toLong()
            formatDateForAxis(timestamp)
        }
    }

    val startAxisFormatter = remember(unit) {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            "%.0f %s".format(value, unit.name.lowercase())
        }
    }

    val axisLabelComponent = textComponent(
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 9.sp,
        typeface = Typeface.DEFAULT
    )

    Chart(
        chart = lineChart(lines = lineSpec),
        chartModelProducer = chartEntryModelProducer,
        startAxis = rememberStartAxis(valueFormatter = startAxisFormatter),
        bottomAxis = rememberBottomAxis(
            label = axisLabelComponent,
            valueFormatter = bottomAxisFormatter,
            tickLength = 4.dp,
            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 3)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun BloodPressureChart(
    entries: List<BloodPressureEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        EmptyChartState("No blood pressure data to display")
        return
    }

    val sortedEntries = remember(entries) { entries.sortedBy { it.timestamp } }
    val systolicColor = MaterialTheme.colorScheme.error
    val diastolicColor = MaterialTheme.colorScheme.tertiary

    val minTimestamp = remember(sortedEntries) { sortedEntries.minOf { it.timestamp } }
    val msPerDay = 24 * 60 * 60 * 1000L

    val chartEntryModelProducer = remember(sortedEntries) {
        ChartEntryModelProducer(
            sortedEntries.map { entry ->
                val dayOffset = ((entry.timestamp - minTimestamp) / msPerDay).toFloat()
                entryOf(dayOffset, entry.systolic.toFloat())
            },
            sortedEntries.map { entry ->
                val dayOffset = ((entry.timestamp - minTimestamp) / msPerDay).toFloat()
                entryOf(dayOffset, entry.diastolic.toFloat())
            }
        )
    }

    val lineSpec = remember(systolicColor, diastolicColor) {
        listOf(
            LineChart.LineSpec(
                lineColor = systolicColor.toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            systolicColor.copy(alpha = 0.2f),
                            systolicColor.copy(alpha = 0f)
                        )
                    )
                )
            ),
            LineChart.LineSpec(
                lineColor = diastolicColor.toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            diastolicColor.copy(alpha = 0.2f),
                            diastolicColor.copy(alpha = 0f)
                        )
                    )
                )
            )
        )
    }

    val bottomAxisFormatter = remember(sortedEntries, minTimestamp) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val timestamp = minTimestamp + (value * msPerDay).toLong()
            formatDateForAxis(timestamp)
        }
    }

    val startAxisFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            "%.0f".format(value)
        }
    }

    val axisLabelComponent = textComponent(
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 9.sp,
        typeface = Typeface.DEFAULT
    )

    Column(modifier = modifier) {
        Chart(
            chart = lineChart(lines = lineSpec),
            chartModelProducer = chartEntryModelProducer,
            startAxis = rememberStartAxis(valueFormatter = startAxisFormatter),
            bottomAxis = rememberBottomAxis(
                label = axisLabelComponent,
                valueFormatter = bottomAxisFormatter,
                tickLength = 4.dp,
                itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 3)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Legend with unit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(color = systolicColor, label = "Systolic")
            Spacer(modifier = Modifier.width(24.dp))
            LegendItem(color = diastolicColor, label = "Diastolic")
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "(mmHg)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CholesterolChart(
    entries: List<CholesterolEntry>,
    modifier: Modifier = Modifier
) {
    val entriesWithTotal = remember(entries) {
        entries.filter { it.total != null }.sortedBy { it.timestamp }
    }

    if (entriesWithTotal.isEmpty()) {
        EmptyChartState("No cholesterol data to display")
        return
    }

    val chartColor = MaterialTheme.colorScheme.secondary

    val minTimestamp = remember(entriesWithTotal) { entriesWithTotal.minOf { it.timestamp } }
    val msPerDay = 24 * 60 * 60 * 1000L

    val chartEntryModelProducer = remember(entriesWithTotal) {
        ChartEntryModelProducer(
            entriesWithTotal.map { entry ->
                val dayOffset = ((entry.timestamp - minTimestamp) / msPerDay).toFloat()
                entryOf(dayOffset, entry.total!!.toFloat())
            }
        )
    }

    val lineSpec = remember(chartColor) {
        listOf(
            LineChart.LineSpec(
                lineColor = chartColor.toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            chartColor.copy(alpha = 0.4f),
                            chartColor.copy(alpha = 0f)
                        )
                    )
                )
            )
        )
    }

    val bottomAxisFormatter = remember(entriesWithTotal, minTimestamp) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val timestamp = minTimestamp + (value * msPerDay).toLong()
            formatDateForAxis(timestamp)
        }
    }

    val startAxisFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            "%.0f".format(value)
        }
    }

    val axisLabelComponent = textComponent(
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 9.sp,
        typeface = Typeface.DEFAULT
    )

    Column(modifier = modifier) {
        Chart(
            chart = lineChart(lines = lineSpec),
            chartModelProducer = chartEntryModelProducer,
            startAxis = rememberStartAxis(valueFormatter = startAxisFormatter),
            bottomAxis = rememberBottomAxis(
                label = axisLabelComponent,
                valueFormatter = bottomAxisFormatter,
                tickLength = 4.dp,
                itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 3)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Unit label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "mg/dL",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SpO2Chart(
    entries: List<SpO2Entry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        EmptyChartState("No SpO2 data to display")
        return
    }

    val sortedEntries = remember(entries) { entries.sortedBy { it.timestamp } }
    val chartColor = MaterialTheme.colorScheme.primary

    val minTimestamp = remember(sortedEntries) { sortedEntries.minOf { it.timestamp } }
    val msPerDay = 24 * 60 * 60 * 1000L

    val chartEntryModelProducer = remember(sortedEntries) {
        ChartEntryModelProducer(
            sortedEntries.map { entry ->
                val dayOffset = ((entry.timestamp - minTimestamp) / msPerDay).toFloat()
                entryOf(dayOffset, entry.spo2.toFloat())
            }
        )
    }

    val lineSpec = remember(chartColor) {
        listOf(
            LineChart.LineSpec(
                lineColor = chartColor.toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            chartColor.copy(alpha = 0.4f),
                            chartColor.copy(alpha = 0f)
                        )
                    )
                )
            )
        )
    }

    val bottomAxisFormatter = remember(sortedEntries, minTimestamp) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val timestamp = minTimestamp + (value * msPerDay).toLong()
            formatDateForAxis(timestamp)
        }
    }

    val startAxisFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            "%.0f%%".format(value)
        }
    }

    val axisLabelComponent = textComponent(
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 9.sp,
        typeface = Typeface.DEFAULT
    )

    Chart(
        chart = lineChart(lines = lineSpec),
        chartModelProducer = chartEntryModelProducer,
        startAxis = rememberStartAxis(valueFormatter = startAxisFormatter),
        bottomAxis = rememberBottomAxis(
            label = axisLabelComponent,
            valueFormatter = bottomAxisFormatter,
            tickLength = 4.dp,
            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 3)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun SpO2SummaryCard(entries: List<SpO2Entry>) {
    if (entries.isEmpty()) return

    val latest = entries.maxByOrNull { it.timestamp }
    val min = entries.minByOrNull { it.spo2 }
    val max = entries.maxByOrNull { it.spo2 }
    val avg = entries.map { it.spo2 }.average()

    SummaryCard(
        title = "SpO2 Summary",
        items = listOf(
            SummaryItem("Latest", latest?.let { "${it.spo2}%" } ?: "-"),
            SummaryItem("Min", min?.let { "${it.spo2}%" } ?: "-"),
            SummaryItem("Max", max?.let { "${it.spo2}%" } ?: "-"),
            SummaryItem("Avg", "%.0f%%".format(avg))
        )
    )
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(2.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = color)
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun WeightSummaryCard(entries: List<WeightEntry>) {
    if (entries.isEmpty()) return

    val latest = entries.maxByOrNull { it.timestamp }
    val min = entries.minByOrNull { it.weight }
    val max = entries.maxByOrNull { it.weight }
    val avg = entries.map { it.weight }.average()
    val unit = latest?.unit ?: WeightUnit.LB

    SummaryCard(
        title = "Weight Summary",
        items = listOf(
            SummaryItem("Latest", latest?.let { "%.1f ${unit.name.lowercase()}".format(it.weight) } ?: "-"),
            SummaryItem("Min", min?.let { "%.1f".format(it.weight) } ?: "-"),
            SummaryItem("Max", max?.let { "%.1f".format(it.weight) } ?: "-"),
            SummaryItem("Avg", "%.1f".format(avg))
        )
    )
}

@Composable
fun BloodPressureSummaryCard(entries: List<BloodPressureEntry>) {
    if (entries.isEmpty()) return

    val latest = entries.maxByOrNull { it.timestamp }
    val avgSystolic = entries.map { it.systolic }.average()
    val avgDiastolic = entries.map { it.diastolic }.average()

    SummaryCard(
        title = "Blood Pressure Summary",
        items = listOf(
            SummaryItem("Latest", latest?.let { "${it.systolic}/${it.diastolic}" } ?: "-"),
            SummaryItem("Avg Sys", "%.0f".format(avgSystolic)),
            SummaryItem("Avg Dia", "%.0f".format(avgDiastolic)),
            SummaryItem("Count", "${entries.size}")
        )
    )
}

@Composable
fun CholesterolSummaryCard(entries: List<CholesterolEntry>) {
    val entriesWithTotal = entries.filter { it.total != null }
    if (entriesWithTotal.isEmpty()) return

    val latest = entriesWithTotal.maxByOrNull { it.timestamp }
    val min = entriesWithTotal.minByOrNull { it.total!! }
    val max = entriesWithTotal.maxByOrNull { it.total!! }
    val avg = entriesWithTotal.mapNotNull { it.total }.average()

    SummaryCard(
        title = "Cholesterol Summary",
        items = listOf(
            SummaryItem("Latest", latest?.total?.toString() ?: "-"),
            SummaryItem("Min", min?.total?.toString() ?: "-"),
            SummaryItem("Max", max?.total?.toString() ?: "-"),
            SummaryItem("Avg", "%.0f".format(avg))
        )
    )
}

private data class SummaryItem(val label: String, val value: String)

@Composable
private fun SummaryCard(title: String, items: List<SummaryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
