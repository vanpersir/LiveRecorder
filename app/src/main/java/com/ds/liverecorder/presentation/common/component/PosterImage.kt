package com.ds.liverecorder.presentation.common.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ds.liverecorder.R
import java.io.File

@Composable
fun PosterImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit // 添加contentScale参数，默认为Fit以完整展示图片
) {
    Card(
        modifier = modifier
    ) {
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            // 尝试加载本地文件图片
            var imageBitmap by remember(imageUrl) { mutableStateOf<ImageBitmap?>(null) }
            
            LaunchedEffect(imageUrl) {
                // 在后台线程加载图片
                try {
                    val file = File(imageUrl)
                    if (file.exists()) {
                        // 首先获取图片尺寸信息
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeFile(imageUrl, options)
                        
                        // 计算缩放比例，将图片缩小到合适尺寸以避免内存溢出
                        val scale = calculateInSampleSize(options, 1024, 1024)
                        
                        // 实际加载图片
                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = scale
                        }
                        val bitmap = BitmapFactory.decodeFile(imageUrl, decodeOptions)
                        imageBitmap = bitmap?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = contentScale // 使用传入的contentScale参数
                )
            } ?: run {
                // 加载失败时显示占位符
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "无法加载图片",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // 占位符
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = contentScale // 使用传入的contentScale参数
                )
            }
        }
    }
}

/**
 * 计算图片缩放比例
 *
 * @param options BitmapFactory.Options 包含原始图片信息
 * @param reqWidth 所需宽度
 * @param reqHeight 所需高度
 * @return 缩放比例
 */
fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // 原始图片的宽度和高度
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        // 计算最大inSampleSize值，使得图片尺寸大于等于目标尺寸
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}