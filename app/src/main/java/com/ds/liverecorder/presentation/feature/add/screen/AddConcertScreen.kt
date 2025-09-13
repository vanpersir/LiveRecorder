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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ds.liverecorder.presentation.common.provider.concertViewModel
import com.ds.liverecorder.presentation.feature.add.component.link.LinkParserDialog
import com.ds.liverecorder.presentation.viewmodel.AddConcertViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConcertScreen(
    onBack: () -> Unit = {},
    onSave: (Concert) -> Unit = {},
    initialConcert: Concert? = null,
    viewModel: AddConcertViewModel = concertViewModel()
) {
    // 初始化演出信息
    LaunchedEffect(initialConcert) {
        viewModel.setInitialConcert(initialConcert)
    }
    
    val uiState by viewModel::uiState
    val isParsingLink = uiState.isParsingLink
    val linkParseError = uiState.linkParseError
    val id = uiState.id
    val title = uiState.title
    val venue = uiState.venue
    val date = uiState.date
    val notes = uiState.notes
    val posterPath = uiState.posterPath
    val ticketPrice = uiState.ticketPrice
    val ticketPriceCurrency = uiState.ticketPriceCurrency
    val actualPaid = uiState.actualPaid
    val actualPaidCurrency = uiState.actualPaidCurrency
    val otherFees = uiState.otherFees
    val otherFeesCurrency = uiState.otherFeesCurrency
    val performers = uiState.performers
    val guests = uiState.guests
    val status = uiState.status
    val category = uiState.category
    val rating = uiState.rating
    
    // 快速添加下拉菜单状态
    var showQuickAddMenu by remember { mutableStateOf(false) }
    // 链接解析弹窗状态
    var showLinkParserDialog by remember { mutableStateOf(false) }
    
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
                val fileName = "posters/poster_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                viewModel.updatePosterPath(file.absolutePath)
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = {
                    // 保存演出
                    // 直接调用onSave回调，避免重复调用viewModel.saveConcert
                    // 创建一个临时的Concert对象用于返回
                    val concert = Concert(
                        id = id,
                        title = title,
                        venue = venue,
                        date = date,
                        notes = notes,
                        posterResId = 0,
                        posterPath = posterPath,
                        ticketPrice = ticketPrice,
                        ticketPriceCurrency = ticketPriceCurrency,
                        actualPaid = actualPaid,
                        actualPaidCurrency = actualPaidCurrency,
                        otherFees = otherFees,
                        otherFeesCurrency = otherFeesCurrency,
                        performers = performers.split(";").map { it.trim() }.filter { it.isNotEmpty() },
                        guests = guests.split(";").map { it.trim() }.filter { it.isNotEmpty() },
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
            
            // 将原来的OutlinedTextField替换为带下拉菜单的按钮
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = { showQuickAddMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("复制/链接/识图/排期等")
                }
                
                DropdownMenu(
                    expanded = showQuickAddMenu,
                    onDismissRequest = { showQuickAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("复制演出") },
                        onClick = { 
                            // TODO: 实现复制演出功能
                            showQuickAddMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("解析链接") },
                        onClick = { 
                            showQuickAddMenu = false
                            showLinkParserDialog = true
                        },
                        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("图片识别") },
                        onClick = { 
                            // TODO: 实现图片识别功能
                            showQuickAddMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Image, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("排期") },
                        onClick = { 
                            // TODO: 实现排期功能
                            showQuickAddMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                    )
                }
            }
            
            // 链接解析弹窗
            if (showLinkParserDialog) {
                LinkParserDialog(
                    onDismissRequest = { showLinkParserDialog = false },
                    onConcertParsed = { sharedText ->
                        viewModel.parseLink(sharedText)
                        showLinkParserDialog = false
                    },
                    isParsing = isParsingLink,
                    errorMessage = linkParseError
                )
            }
            
            Text(
                text = "基础信息",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("演出名称") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = venue,
                onValueChange = { viewModel.updateVenue(it) },
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
                    onDateSelected = { viewModel.updateDate(it) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                
                TimePickerField(
                    label = "时间",
                    value = date,
                    onTimeSelected = { viewModel.updateDate(it) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
            
            StatusSelector(
                label = "状态",
                value = status,
                onValueChange = { viewModel.updateStatus(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            CategorySelector(
                label = "分类",
                value = category,
                onValueChange = { viewModel.updateCategory(it) },
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
                onValueChange = { viewModel.updatePerformers(it) },
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
                onValueChange = { viewModel.updateGuests(it) },
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
                onRatingChanged = { viewModel.updateRating(it) },
                isEditable = true,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            CurrencyField(
                label = "票价",
                value = ticketPrice,
                onValueChange = { viewModel.updateTicketPrice(it) },
                currency = ticketPriceCurrency,
                onCurrencyChange = { viewModel.updateTicketPriceCurrency(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            CurrencyField(
                label = "实付",
                value = actualPaid,
                onValueChange = { viewModel.updateActualPaid(it) },
                currency = actualPaidCurrency,
                onCurrencyChange = { viewModel.updateActualPaidCurrency(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            CurrencyField(
                label = "其他费用",
                value = otherFees,
                onValueChange = { viewModel.updateOtherFees(it) },
                currency = otherFeesCurrency,
                onCurrencyChange = { viewModel.updateOtherFeesCurrency(it) },
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
                onValueChange = { viewModel.updateNotes(it) },
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