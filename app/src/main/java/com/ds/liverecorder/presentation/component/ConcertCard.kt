package com.ds.liverecorder.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.data.entity.ConcertEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun ConcertCard(
    title: String,
    venue: String,
    date: Date,
    status: String,
    posterPath: String? = null,
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
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 使用PosterImage组件展示海报
            PosterImage(
                imageUrl = posterPath,
                contentDescription = "演出海报",
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Text(
                    text = venue,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = daysText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(4.dp)
                    )
                    
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConcertCardPreview() {
    ConcertCard(
        title = "演出名称",
        venue = "演出场地",
        date = Date(),
        status = "待看"
    )
}