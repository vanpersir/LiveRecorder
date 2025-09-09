package com.ds.liverecorder.data.repository

import android.content.Context
import android.util.Log
import com.ds.liverecorder.data.remote.DamaiApiService
import com.ds.liverecorder.data.remote.ShowStartWebViewLoader
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.repository.ConcertLinkParserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * 演出链接解析仓库实现
 * 负责实际的链接解析逻辑，包括网络请求和数据解析
 * @param context 应用上下文，用于WebView加载
 */
class ConcertLinkParserRepositoryImpl(private val context: Context) : ConcertLinkParserRepository {
    
    companion object {
        private const val TAG = "ConcertLinkParser"
    }
    
    override suspend fun parseLink(link: String): Concert? {
        return when {
            link.contains("showstart.com") -> parseShowStartLink(link)
            link.contains("damai.cn") -> parseDamaiLink(link)
            else -> null
        }
    }
    
    /**
     * 解析秀动链接
     * @param link 秀动链接
     * @return 解析出的演出信息
     */
    private suspend fun parseShowStartLink(link: String): Concert? {
        Log.d(TAG, "Parsing ShowStart link: $link")
        return try {
            // 首先尝试使用WebView方式解析
            Log.d(TAG, "Attempting to parse with WebView")
            parseShowStartLinkWithWebView(link)
        } catch (e: Exception) {
            Log.e(TAG, "ShowStart WebView parsing failed", e)
        } as Concert?
    }
    
    /**
     * 使用WebView解析秀动链接
     * @param link 秀动链接
     * @return 解析出的演出信息
     */
    private suspend fun parseShowStartLinkWithWebView(link: String): Concert? {
        Log.d(TAG, "Starting ShowStart WebView parsing")
        val webViewLoader = ShowStartWebViewLoader(context)
        val result = webViewLoader.parseLinkWithWebView(link)
        Log.d(TAG, "WebView parsing result: $result")
        return result
    }

    /**
     * 解析大麦链接
     * @param link 大麦链接
     * @return 解析出的演出信息
     */
    private suspend fun parseDamaiLink(link: String): Concert? {
        return try {
            // 从链接中提取项目ID
            val itemId = extractDamaiItemId(link) ?: return null
            
            // 在IO线程中执行网络请求
            withContext(Dispatchers.IO) {
                // 创建OkHttpClient并设置超时时间
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                // 创建API服务
                val apiService = Retrofit.Builder()
                    .baseUrl("https://m.damai.cn/api/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(DamaiApiService::class.java)
                
                // 发起网络请求获取演出详情
                val call = apiService.getItemDetail(itemId)
                val response: Response<ResponseBody> = call.execute() as Response<ResponseBody>
                
                // 检查响应是否成功
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // 检查响应内容类型
                        val contentType = response.headers()["Content-Type"]
                        if (contentType != null && contentType.contains("application/json")) {
                            // 注意：这里应该解析真实的API响应
                            // 由于我们没有真实的API结构，暂时返回模拟数据
                            createMockDamaiConcert(itemId, link)
                        } else {
                            // 如果响应不是JSON，回退到从链接文本中提取信息
                            parseDamaiFromText(link, itemId)
                        }
                    } else {
                        // 如果响应体为空，回退到从链接文本中提取信息
                        parseDamaiFromText(link, itemId)
                    }
                } else {
                    // 检查错误响应体
                    val errorBody = response.errorBody()
                    if (errorBody != null) {
                        val errorBodyString = errorBody.string()
                        // 如果错误响应体包含HTML内容，说明可能跳转到了网页
                        if (errorBodyString.contains("<html") || errorBodyString.contains("<!DOCTYPE")) {
                            // 回退到从链接文本中提取信息
                            parseDamaiFromText(link, itemId)
                        } else {
                            // 其他错误情况，也回退到从链接文本中提取信息
                            parseDamaiFromText(link, itemId)
                        }
                    } else {
                        // 如果没有错误响应体，回退到从链接文本中提取信息
                        parseDamaiFromText(link, itemId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果网络请求失败，回退到从链接文本中提取信息
            val itemId = extractDamaiItemId(link) ?: return null
            return parseDamaiFromText(link, itemId)
        }
    }

    /**
     * 从大麦链接中提取项目ID
     * @param link 大麦链接
     * @return 项目ID，如果无法提取则返回null
     */
    private fun extractDamaiItemId(link: String): String? {
        // 匹配 itemId 参数
        val itemIdRegex = Regex("[?&]itemId=(\\d+)")
        val itemIdMatch = itemIdRegex.find(link)
        if (itemIdMatch != null) {
            return itemIdMatch.groupValues[1]
        }
        
        return null
    }

    /**
     * 创建大麦演出的模拟数据
     * @param itemId 项目ID
     * @param link 原始链接
     * @return 模拟的演出信息
     */
    private fun createMockDamaiConcert(itemId: String, link: String): Concert {
        val date = Date()
        
        return Concert(
            id = 0,
            title = "大麦演出 - 项目ID: $itemId",
            venue = "未知场地",
            date = date,
            notes = "从大麦链接解析的演出信息\n链接: $link\n项目ID: $itemId",
            posterResId = 0,
            posterPath = "",
            ticketPrice = "",
            ticketPriceCurrency = "CNY",
            actualPaid = "",
            actualPaidCurrency = "CNY",
            otherFees = "",
            otherFeesCurrency = "CNY",
            performers = listOf("未知演出者"),
            guests = emptyList(),
            status = "待看",
            category = "演唱会",
            rating = 0
        )
    }

    /**
     * 从大麦链接文本中提取演出信息（回退方案）
     * @param link 链接文本
     * @param itemId 项目ID
     * @return 演出信息
     */
    private fun parseDamaiFromText(link: String, itemId: String): Concert? {
        // 从链接中提取演出信息（简单实现）
        val date = Date()
        
        // 生成演出信息
        return Concert(
            id = 0,
            title = "大麦演出 - 项目ID: $itemId",
            venue = "未知场地",
            date = date,
            notes = "从大麦链接解析的演出信息\n链接: $link\n项目ID: $itemId",
            posterResId = 0,
            posterPath = "",
            ticketPrice = "",
            ticketPriceCurrency = "CNY",
            actualPaid = "",
            actualPaidCurrency = "CNY",
            otherFees = "",
            otherFeesCurrency = "CNY",
            performers = listOf("未知演出者"),
            guests = emptyList(),
            status = "待看",
            category = "演唱会",
            rating = 0
        )
    }

}