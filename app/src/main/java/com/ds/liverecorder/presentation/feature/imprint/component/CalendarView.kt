package com.ds.liverecorder.presentation.feature.imprint.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.common.component.PosterImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarView(
    concerts: List<Concert>,
    onConcertClick: (Concert) -> Unit = {}
) {
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    // 滑动阈值，当拖动距离超过这个值时才切换月份
    val swipeThreshold = 100f
    var dragAmount by remember { mutableFloatStateOf(0f) }
    
    // 创建切换月份的回调函数
    val onPreviousMonth = {
        val newDate = currentDate.clone() as Calendar
        newDate.add(Calendar.MONTH, -1)
        currentDate = newDate
    }
    
    val onNextMonth = {
        val newDate = currentDate.clone() as Calendar
        newDate.add(Calendar.MONTH, 1)
        currentDate = newDate
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        CalendarHeader(
            calendar = currentDate,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )
        
        // 添加水平滑动支持到日历主体部分
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .draggable(
                    state = rememberDraggableState { delta ->
                        dragAmount += delta
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        // 根据拖动距离决定是否切换月份
                        if (dragAmount > swipeThreshold) {
                            // 向右滑动，切换到上一个月
                            onPreviousMonth()
                        } else if (dragAmount < -swipeThreshold) {
                            // 向左滑动，切换到下一个月
                            onNextMonth()
                        }
                        // 重置拖动距离
                        dragAmount = 0f
                    }
                )
        ) {
            Column {
                CalendarDayNames()
                CalendarDays(
                    currentDate = currentDate,
                    concerts = concerts,
                    onConcertClick = onConcertClick
                )
            }
        }
        
        UpcomingConcertsList(
            concerts = concerts, 
            currentMonth = currentDate,
            onConcertClick = onConcertClick
        )
    }
}

@Composable
fun CalendarHeader(
    calendar: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onPreviousMonth)
                .padding(8.dp)
        ) {
            Text(
                text = "◀",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Text(
            text = SimpleDateFormat("yyyy年MM月", Locale.getDefault()).format(calendar.time),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Box(
            modifier = Modifier
                .clickable(onClick = onNextMonth)
                .padding(8.dp)
        ) {
            Text(
                text = "▶",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun CalendarDayNames() {
    val days = listOf("日", "一", "二", "三", "四", "五", "六")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarDays(
    currentDate: Calendar,
    concerts: List<Concert>,
    onConcertClick: (Concert) -> Unit
) {
    val calendar = currentDate.clone() as Calendar
    
    // 设置到月份的第一天
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    // 获取该月第一天是星期几
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 转换为从0开始（周日为0）
    
    // 获取该月总天数
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // 创建一个包含该月所有日期的列表，包括前后月份的填充日期
    val days = mutableListOf<Calendar?>()
    
    // 添加前一个月的填充日期
    val prevMonthCalendar = calendar.clone() as Calendar
    prevMonthCalendar.add(Calendar.MONTH, -1)
    val daysInPrevMonth = prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    for (i in (firstDayOfWeek - 1) downTo 0) {
        val dayCalendar = prevMonthCalendar.clone() as Calendar
        dayCalendar.set(Calendar.DAY_OF_MONTH, daysInPrevMonth - i)
        days.add(dayCalendar)
    }
    
    // 添加当前月的所有日期
    for (day in 1..daysInMonth) {
        val dayCalendar = calendar.clone() as Calendar
        dayCalendar.set(Calendar.DAY_OF_MONTH, day)
        days.add(dayCalendar)
    }
    
    // 计算需要多少天来填满6行7列的网格
    val totalCells = 42 // 6行 × 7列
    val remainingCells = totalCells - days.size
    
    // 添加下一个月的填充日期
    val nextMonthCalendar = calendar.clone() as Calendar
    nextMonthCalendar.add(Calendar.MONTH, 1)
    
    for (day in 1..remainingCells) {
        val dayCalendar = nextMonthCalendar.clone() as Calendar
        dayCalendar.set(Calendar.DAY_OF_MONTH, day)
        days.add(dayCalendar)
    }
    
    // 显示日历网格
    Column {
        for (week in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayIndex in 0 until 7) {
                    val dayCalendar = days[week * 7 + dayIndex]
                    if (dayCalendar != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            val dayConcerts = concerts.filter { concert ->
                                isSameDay(dayCalendar.time, concert.date)
                            }
                            
                            CalendarDay(
                                dayCalendar = dayCalendar,
                                isCurrentMonth = dayCalendar.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH),
                                concerts = dayConcerts,
                                onClick = { concert ->
                                    // 当点击海报时，触发对应演出的点击事件
                                    onConcertClick(concert)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    dayCalendar: Calendar,
    isCurrentMonth: Boolean,
    concerts: List<Concert>,
    onClick: (Concert) -> Unit
) {
    val hasConcerts = concerts.isNotEmpty()
    val isToday = isSameDay(dayCalendar.time, Calendar.getInstance().time)
    
    // 用于循环播放海报的索引状态
    var currentIndex by remember(concerts) { mutableIntStateOf(0) }
    
    // 使用LaunchedEffect定时切换海报
    if (hasConcerts && concerts.size > 1) {
        LaunchedEffect(dayCalendar) {
            while (true) {
                delay(2000) // 每2秒切换一次海报
                currentIndex = (currentIndex + 1) % concerts.size
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    if (hasConcerts) {
                        onClick(concerts[currentIndex])
                    }
                }
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 日期文本
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isToday) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString(),
                color = if (isCurrentMonth) {
                    if (isToday) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outline
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // 如果有演出，在日期上覆盖显示海报
        if (hasConcerts) {
            val concert = concerts[currentIndex]
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        // 点击海报时，调用onConcertClick回调并传递当前演出
                        onClick(concert)
                    }
            ) {
                PosterImage(
                    imageUrl = concert.posterPath,
                    contentDescription = "演出海报: ${concert.title}",
                    modifier = Modifier
                        .size(32.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun UpcomingConcertsList(
    concerts: List<Concert>,
    currentMonth: Calendar,
    onConcertClick: (Concert) -> Unit
) {
    val upcomingConcerts = concerts.filter { concert ->
        concert.date.after(Date()) && 
        concert.status == "正常" && 
        isSameMonth(concert.date, currentMonth.time)
    }.sortedBy { it.date }
    
    if (upcomingConcerts.isNotEmpty()) {
        Text(
            text = "即将到来的演出",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            items(upcomingConcerts) { concert ->
                ConcertItem(
                    concert = concert,
                    onClick = { onConcertClick(concert) }
                )
            }
        }
    }
}

@Composable
fun ConcertItem(
    concert: Concert,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = concert.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = SimpleDateFormat("MM-dd HH:mm EEEE", Locale.getDefault()).format(concert.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = concert.venue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = date1
    cal2.time = date2
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isSameMonth(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = date1
    cal2.time = date2
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

@Preview(showBackground = true)
@Composable
fun CalendarViewPreview() {
    val concerts = listOf(
        Concert(
            id = 1,
            title = "音乐会",
            venue = "国家大剧院",
            date = Date(),
            notes = "",
            posterResId = 0,
            posterPath = "",
            ticketPrice = "500",
            ticketPriceCurrency = "CNY",
            actualPaid = "500",
            actualPaidCurrency = "CNY",
            otherFees = "0",
            otherFeesCurrency = "CNY",
            performers = listOf(),
            guests = listOf(),
            status = "正常",
            category = "音乐",
            rating = 5
        )
    )

    CalendarView(concerts = concerts)
}