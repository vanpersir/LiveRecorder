package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun ConcertTimelineItem(
    title: String,
    venue: String,
    date: Date,
    status: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val oneDayInMillis = 24 * 60 * 60 * 1000L
    val daysDiff = ((date.time - System.currentTimeMillis()) / oneDayInMillis).toInt()
    val daysText = when {
        daysDiff > 0 -> "${abs(daysDiff)}天后"
        daysDiff < 0 -> "${abs(daysDiff)}天前"
        else -> "今天"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = venue,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConcertTimelineItemPreview() {
    ConcertTimelineItem(
        title = "演出名称",
        venue = "演出场地",
        date = Date(),
        status = "待看"
    )
}