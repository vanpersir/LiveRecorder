package com.ds.liverecorder.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date

/**
 * 图片下载工具类
 */
object ImageDownloader {
    private const val TAG = "ImageDownloader"
    
    /**
     * 从网络URL下载图片并保存到本地
     *
     * @param imageUrl 图片的网络URL
     * @param directory 保存图片的目录
     * @param fileName 保存的文件名，如果为空则自动生成
     * @return 保存的文件路径，如果失败则返回null
     */
    suspend fun downloadAndSaveImage(
        imageUrl: String,
        directory: File,
        fileName: String? = null
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            // 检查URL是否为空
            if (imageUrl.isBlank()) {
                Log.w(TAG, "Image URL is blank")
                return@withContext null
            }
            
            // 创建目录
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            // 生成文件名
            val actualFileName = fileName ?: generateFileName(imageUrl)
            val file = File(directory, actualFileName)
            
            // 下载图片
            val bitmap = downloadImage(imageUrl)
            if (bitmap != null) {
                // 保存图片
                saveBitmapToFile(bitmap, file)
                // 确保返回的是完整的绝对路径
                file.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading and saving image from: $imageUrl", e)
            null
        }
    }
    
    /**
     * 从URL下载图片
     *
     * @param imageUrl 图片URL
     * @return 下载的Bitmap，如果失败则返回null
     */
    private fun downloadImage(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000 // 10秒连接超时
            connection.readTimeout = 10000 // 10秒读取超时
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.connect()
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                connection.disconnect()
                bitmap
            } else {
                Log.e(TAG, "Failed to download image. Response code: ${connection.responseCode}")
                connection.disconnect()
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image from: $imageUrl", e)
            null
        }
    }
    
    /**
     * 保存Bitmap到文件
     *
     * @param bitmap 要保存的Bitmap
     * @param file 目标文件
     */
    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.flush()
        }
    }
    
    /**
     * 根据URL生成文件名
     *
     * @param imageUrl 图片URL
     * @return 生成的文件名
     */
    private fun generateFileName(imageUrl: String): String {
        // 从URL中提取文件扩展名
        val extension = getFileExtension(imageUrl) ?: "jpg"
        
        // 使用时间戳生成唯一文件名
        val timestamp = Date().time
        return "poster_$timestamp.$extension"
    }
    
    /**
     * 从URL中提取文件扩展名
     *
     * @param url URL
     * @return 文件扩展名，如果没有则返回null
     */
    private fun getFileExtension(url: String): String? {
        return try {
            val path = URL(url).path
            val lastDotIndex = path.lastIndexOf('.')
            if (lastDotIndex != -1) {
                path.substring(lastDotIndex + 1)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract file extension from URL: $url", e)
            null
        }
    }
}