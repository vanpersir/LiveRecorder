package com.ds.liverecorder.presentation.feature.add.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.common.component.CategorySelector
import com.ds.liverecorder.presentation.common.component.CurrencyField
import com.ds.liverecorder.presentation.common.component.DatePickerField
import com.ds.liverecorder.presentation.common.component.PosterImage
import com.ds.liverecorder.presentation.common.component.RatingBar
import com.ds.liverecorder.presentation.common.component.StatusSelector
import com.ds.liverecorder.presentation.common.component.TimePickerField
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConcertScreen(
    onBack: () -> Unit = {},
    onSave: (Concert) -> Unit = {},
    initialConcert: Concert? = null
) {
    var title by remember { mutableStateOf(initialConcert?.title ?: "") }
    var venue by remember { mutableStateOf(initialConcert?.venue ?: "") }
    var date by remember { mutableStateOf(initialConcert?.date ?: Date()) }
    var notes by remember { mutableStateOf(initialConcert?.notes ?: "") }
    var ticketPrice by remember { mutableStateOf(initialConcert?.ticketPrice ?: "") }
    var actualPaid by remember { mutableStateOf(initialConcert?.actualPaid ?: "") }
    var otherFees by remember { mutableStateOf(initialConcert?.otherFees ?: "") }
    var ticketPriceCurrency by remember { mutableStateOf(initialConcert?.ticketPriceCurrency ?: "CNY") }
    var actualPaidCurrency by remember { mutableStateOf(initialConcert?.actualPaidCurrency ?: "CNY") }
    var otherFeesCurrency by remember { mutableStateOf(initialConcert?.otherFeesCurrency ?: "CNY") }
    var status by remember { mutableStateOf(initialConcert?.status ?: "") }
    var category by remember { mutableStateOf(initialConcert?.category ?: "") }
    var performers by remember { mutableStateOf(initialConcert?.performers?.joinToString(", ") ?: "") }
    var guests by remember { mutableStateOf(initialConcert?.guests?.joinToString(", ") ?: "") }
    var rating by remember { mutableStateOf(initialConcert?.rating ?: 0) }
    var posterPath by remember { mutableStateOf(initialConcert?.posterPath ?: "") }
    
    val context = LocalContext.current

    // 图片选择器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 保存图片到内部存储
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                
                // 创建文件保存图片
                val fileName = "poster_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                posterPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    val isEditMode = initialConcert != null
    val screenTitle = if (isEditMode) "编辑演出" else "添加演出"

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(screenTitle) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = {
                    val concert = Concert(
                        id = initialConcert?.id ?: 0,
                        title = title,
                        venue = venue,
                        date = date,
                        notes = notes,
                        posterResId = initialConcert?.posterResId ?: 0,
                        posterPath = posterPath,
                        ticketPrice = ticketPrice,
                        ticketPriceCurrency = ticketPriceCurrency,
                        actualPaid = actualPaid,
                        actualPaidCurrency = actualPaidCurrency,
                        otherFees = otherFees,
                        otherFeesCurrency = otherFeesCurrency,
                        performers = performers.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        guests = guests.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        status = status,
                        category = category,
                        rating = rating
                    )
                    onSave(concert)
                }) {
                    Icon(Icons.Filled.Check, contentDescription = "保存")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "快速添加提示",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("复制/链接/识图/排期等") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "基础信息",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("演出名称") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                label = { Text("场地") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 将日期和时间选择器放在同一行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DatePickerField(
                    label = "日期",
                    value = date,
                    onDateSelected = { date = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                
                TimePickerField(
                    label = "时间",
                    value = date,
                    onTimeSelected = { date = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
            
            StatusSelector(
                label = "状态",
                value = status,
                onValueChange = { status = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            CategorySelector(
                label = "分类",
                value = category,
                onValueChange = { category = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 显示海报预览（如果已选择）
            if (posterPath.isNotEmpty()) {
                PosterImage(
                    imageUrl = posterPath,
                    contentDescription = "演出海报预览",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            
            Button(
                onClick = { 
                    launcher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(if (posterPath.isNotEmpty()) "更改海报" else "添加海报")
            }
            
            Text(
                text = "阵容",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = performers,
                onValueChange = { performers = it },
                label = { Text("添加演出者") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("添加演出者")
            }
            
            Text(
                text = "嘉宾",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = guests,
                onValueChange = { guests = it },
                label = { Text("添加嘉宾") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("添加嘉宾")
            }
            
            Text(
                text = "我的评价",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            RatingBar(
                rating = rating,
                onRatingChanged = { rating = it },
                isEditable = true,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            CurrencyField(
                label = "票价",
                value = ticketPrice,
                onValueChange = { ticketPrice = it },
                currency = ticketPriceCurrency,
                onCurrencyChange = { ticketPriceCurrency = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            CurrencyField(
                label = "实付",
                value = actualPaid,
                onValueChange = { actualPaid = it },
                currency = actualPaidCurrency,
                onCurrencyChange = { actualPaidCurrency = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            CurrencyField(
                label = "其他费用",
                value = otherFees,
                onValueChange = { otherFees = it },
                currency = otherFeesCurrency,
                onCurrencyChange = { otherFeesCurrency = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("座位号") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                maxLines = 3
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddConcertScreenPreview() {
    AddConcertScreen()
}