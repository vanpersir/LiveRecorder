package com.ds.liverecorder.presentation.feature.record.component

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.presentation.common.component.PosterImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun ConcertPoster(
    title: String,
    date: Date,
    venue: String,
    posterPath: String? = null,
    @SuppressLint("ModifierParameter")
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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 使用PosterImage组件展示海报
            PosterImage(
                imageUrl = posterPath, // 传递实际的海报路径
                contentDescription = "演出海报",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // 设置固定高度以确保海报展示的一致性
                contentScale = ContentScale.Crop // 使用Crop模式确保海报填满空间
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = venue,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.Start)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConcertPosterPreview() {
    ConcertPoster(
        title = "演出名称",
        date = Date(),
        venue = "演出场地"
    )
}