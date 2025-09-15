package com.ds.liverecorder.presentation.common.component

import androidx.compose.runtime.Composable
import java.util.Date
import kotlin.math.abs

@Composable
fun dateDiffText(date: Date): String {
    val oneDayInMillis = 24 * 60 * 60 * 1000L
    val oneHourInMillis = 60 * 60 * 1000L
    val timeDiff = date.time - System.currentTimeMillis()
    val daysDiff = (timeDiff / oneDayInMillis).toInt()
    val hoursDiff = (timeDiff / oneHourInMillis).toInt()

    return when {
        daysDiff > 0 -> "${abs(daysDiff)}天后"
        daysDiff < 0 -> "${abs(daysDiff)}天前"
        hoursDiff > 0 -> "${abs(hoursDiff)}小时后"
        hoursDiff < 0 -> "${abs(hoursDiff)}小时前"
        else -> "今天"
    }
}