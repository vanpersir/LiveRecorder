package com.ds.liverecorder.data.repository

import android.content.Context
import android.util.Log
import com.ds.liverecorder.data.remote.DamaiApiService
import com.ds.liverecorder.data.remote.ShowStartActivityResponse
import com.ds.liverecorder.data.remote.ShowStartApiService
import com.ds.liverecorder.data.remote.ShowStartRealApiService
import com.ds.liverecorder.data.remote.ShowStartWebViewLoader
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.repository.ConcertLinkParserRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
            // WebView解析失败，回退到原来的API方式
            Log.d(TAG, "Falling back to API parsing method")
            parseShowStartLinkWithApi(link)
        }
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
     * 使用API解析秀动链接（原有方法）
     * @param link 秀动链接
     * @return 解析出的演出信息
     */
    private suspend fun parseShowStartLinkWithApi(link: String): Concert? {
        return try {
            // 从链接中提取活动ID
            val activityId = extractShowStartActivityId(link) ?: return null
            
            // 在IO线程中执行网络请求
            withContext(Dispatchers.IO) {
                // 创建OkHttpClient并设置超时时间
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                // 首先尝试使用真实的API接口
                val realApiService = Retrofit.Builder()
                    .baseUrl("https://wap.showstart.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ShowStartRealApiService::class.java)
                
                // 发起网络请求获取演出详情（真实API）
                val realCall = realApiService.getActivityDetail(activityId)
                val realResponse: Response<ResponseBody> = realCall.execute()
                
                // 打印真实API响应信息用于调试
                Log.d(TAG, "ShowStart Real API Response - Code: ${realResponse.code()}, Message: ${realResponse.message()}")
                
                // 检查真实API响应是否成功
                if (realResponse.isSuccessful) {
                    val responseBody = realResponse.body()
                    if (responseBody != null) {
                        // 打印响应头信息
                        Log.d(TAG, "ShowStart Real API Response Headers: ${realResponse.headers()}")
                        
                        // 检查响应内容类型
                        val contentType = realResponse.headers()["Content-Type"]
                        Log.d(TAG, "ShowStart Real API Content-Type: $contentType")
                        
                        if (contentType != null && contentType.contains("application/json")) {
                            // 尝试解析JSON响应
                            try {
                                // 获取响应体字符串并打印
                                val responseString = responseBody.string()
                                
                                // 打印完整的响应体
                                Log.d(TAG, "ShowStart Real API Response Body Length: ${responseString.length}")
                                if (responseString.length <= 3000) {
                                    Log.d(TAG, "ShowStart Real API Response Body: $responseString")
                                } else {
                                    Log.d(TAG, "ShowStart Real API Response Body (first 1500 chars): ${responseString.take(1500)}")
                                    Log.d(TAG, "ShowStart Real API Response Body (last 1500 chars): ${responseString.takeLast(1500)}")
                                }
                                
                                // 尝试解析为ShowStartActivityResponse对象
                                try {
                                    val gson = Gson()
                                    // 使用Gson解析响应
                                    val response = gson.fromJson(responseString, ShowStartActivityResponse::class.java)
                                    return@withContext createConcertFromShowStartResponse(response, link)
                                } catch (e: Exception) {
                                    Log.e(TAG, "ShowStart Real API JSON parsing failed", e)
                                    // JSON解析失败，使用模拟数据
                                    return@withContext createMockShowStartConcert(activityId, link)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "ShowStart Real API Response body reading failed", e)
                                // 读取响应体失败，回退到从链接文本中提取信息
                                return@withContext parseShowStartFromText(link, activityId)
                            }
                        } else {
                            // 如果响应不是JSON，尝试使用原来的API
                            Log.d(TAG, "ShowStart Real API Response is not JSON, trying original API")
                            return@withContext tryOriginalShowStartApi(client, activityId, link)
                        }
                    } else {
                        // 如果响应体为空，尝试使用原来的API
                        Log.d(TAG, "ShowStart Real API Response Body is null, trying original API")
                        return@withContext tryOriginalShowStartApi(client, activityId, link)
                    }
                } else {
                    // 打印错误响应信息
                    Log.e(TAG, "ShowStart Real API Request failed - Code: ${realResponse.code()}, Message: ${realResponse.message()}")
                    
                    // 真实API请求失败，尝试使用原来的API
                    return@withContext tryOriginalShowStartApi(client, activityId, link)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ShowStart API Request exception", e)
            // 如果网络请求失败，回退到从链接文本中提取信息
            val activityId = extractShowStartActivityId(link) ?: return null
            return parseShowStartFromText(link, activityId)
        }
    }
    
    /**
     * 尝试使用原来的秀动API
     */
    private suspend fun tryOriginalShowStartApi(client: OkHttpClient, activityId: String, link: String): Concert? {
        return withContext(Dispatchers.IO) {
            try {
                // 创建原来的API服务
                val apiService = Retrofit.Builder()
                    .baseUrl("https://wap.showstart.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ShowStartApiService::class.java)
                
                // 发起网络请求获取演出详情
                val call = apiService.getActivityDetail(activityId)
                val response: Response<ResponseBody> = call.execute()
                
                // 打印响应信息用于调试
                Log.d(TAG, "ShowStart Original API Response - Code: ${response.code()}, Message: ${response.message()}")
                
                // 检查响应是否成功
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // 打印响应头信息
                        Log.d(TAG, "ShowStart Original API Response Headers: ${response.headers()}")
                        
                        // 检查响应内容类型
                        val contentType = response.headers()["Content-Type"]
                        Log.d(TAG, "ShowStart Original API Content-Type: $contentType")
                        
                        if (contentType != null && contentType.contains("application/json")) {
                            // 尝试解析JSON响应
                            try {
                                // 获取响应体字符串并打印
                                val responseString = responseBody.string()
                                
                                // 打印完整的响应体
                                Log.d(TAG, "ShowStart Original API Response Body Length: ${responseString.length}")
                                if (responseString.length <= 3000) {
                                    Log.d(TAG, "ShowStart Original API Response Body: $responseString")
                                } else {
                                    Log.d(TAG, "ShowStart Original API Response Body (first 1500 chars): ${responseString.take(1500)}")
                                    Log.d(TAG, "ShowStart Original API Response Body (last 1500 chars): ${responseString.takeLast(1500)}")
                                }
                                
                                // 尝试解析为ShowStartActivityResponse对象
                                try {
                                    val gson = Gson()
                                    // 使用Gson解析响应
                                    val response = gson.fromJson(responseString, ShowStartActivityResponse::class.java)
                                    return@withContext createConcertFromShowStartResponse(response, link)
                                } catch (e: Exception) {
                                    Log.e(TAG, "ShowStart Original API JSON parsing failed", e)
                                    // JSON解析失败，使用模拟数据
                                    return@withContext createMockShowStartConcert(activityId, link)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "ShowStart Original API Response body reading failed", e)
                                // 读取响应体失败，回退到从链接文本中提取信息
                                return@withContext parseShowStartFromText(link, activityId)
                            }
                        } else {
                            // 如果响应不是JSON，尝试解析HTML内容
                            Log.d(TAG, "ShowStart Original API Response is not JSON, trying HTML parsing")
                            return@withContext tryHtmlParsing(responseBody, link, activityId)
                        }
                    } else {
                        // 如果响应体为空，回退到从链接文本中提取信息
                        Log.d(TAG, "ShowStart Original API Response Body is null")
                        return@withContext parseShowStartFromText(link, activityId)
                    }
                } else {
                    // 打印错误响应信息
                    Log.e(TAG, "ShowStart Original API Request failed - Code: ${response.code()}, Message: ${response.message()}")
                    
                    // 检查错误响应体
                    val errorBody = response.errorBody()
                    if (errorBody != null) {
                        val errorBodyString = errorBody.string()
                        
                        // 打印完整的错误响应体
                        Log.d(TAG, "ShowStart Original API Error Body Length: ${errorBodyString.length}")
                        if (errorBodyString.length <= 5000) {
                            Log.d(TAG, "ShowStart Original API Error Body: $errorBodyString")
                        } else {
                            Log.d(TAG, "ShowStart Original API Error Body (first 2500 chars): ${errorBodyString.take(2500)}")
                            Log.d(TAG, "ShowStart Original API Error Body (last 2500 chars): ${errorBodyString.takeLast(2500)}")
                        }
                        
                        // 如果错误响应体包含HTML内容，尝试解析HTML
                        if (errorBodyString.contains("<html") || errorBodyString.contains("<!DOCTYPE")) {
                            Log.d(TAG, "ShowStart Original API Error Body contains HTML, trying HTML parsing")
                            return@withContext tryHtmlParsingFromString(errorBodyString, link, activityId)
                        } else {
                            // 其他错误情况，也回退到从链接文本中提取信息
                            return@withContext parseShowStartFromText(link, activityId)
                        }
                    } else {
                        // 如果没有错误响应体，回退到从链接文本中提取信息
                        Log.d(TAG, "ShowStart Original API Error Body is null")
                        return@withContext parseShowStartFromText(link, activityId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "ShowStart Original API Request exception", e)
                // 如果网络请求失败，回退到从链接文本中提取信息
                return@withContext parseShowStartFromText(link, activityId)
            }
        }
    }
    
    /**
     * 尝试解析HTML内容
     */
    private fun tryHtmlParsing(responseBody: ResponseBody, link: String, activityId: String): Concert? {
        try {
            val htmlContent = responseBody.string()
            Log.d(TAG, "ShowStart HTML Content Length: ${htmlContent.length}")
            
            // 打印完整的HTML内容
            if (htmlContent.length <= 5000) {
                Log.d(TAG, "ShowStart HTML Content: $htmlContent")
            } else {
                Log.d(TAG, "ShowStart HTML Content (first 2500 chars): ${htmlContent.take(2500)}")
                Log.d(TAG, "ShowStart HTML Content (last 2500 chars): ${htmlContent.takeLast(2500)}")
            }
            
            return parseHtmlContent(htmlContent, link, activityId)
        } catch (e: Exception) {
            Log.e(TAG, "ShowStart HTML parsing failed", e)
            return parseShowStartFromText(link, activityId)
        }
    }
    
    /**
     * 尝试解析HTML字符串内容
     */
    private fun tryHtmlParsingFromString(htmlContent: String, link: String, activityId: String): Concert? {
        try {
            Log.d(TAG, "ShowStart HTML Content Length: ${htmlContent.length}")
            
            // 打印完整的HTML内容
            if (htmlContent.length <= 5000) {
                Log.d(TAG, "ShowStart HTML Content: $htmlContent")
            } else {
                Log.d(TAG, "ShowStart HTML Content (first 2500 chars): ${htmlContent.take(2500)}")
                Log.d(TAG, "ShowStart HTML Content (last 2500 chars): ${htmlContent.takeLast(2500)}")
            }
            
            return parseHtmlContent(htmlContent, link, activityId)
        } catch (e: Exception) {
            Log.e(TAG, "ShowStart HTML parsing failed", e)
            return parseShowStartFromText(link, activityId)
        }
    }
    
    /**
     * 解析HTML内容提取演出信息
     */
    private fun parseHtmlContent(htmlContent: String, link: String, activityId: String): Concert? {
        try {
            // 使用正则表达式从HTML中提取信息
            // 由于秀动使用的是SPA，大部分内容是通过JavaScript动态加载的，
            // 所以我们无法直接从静态HTML中提取详细信息
            
            // 使用从链接文本中提取的信息作为备选
            val linkTitle = extractShowStartTitle(link)
            val linkVenue = extractShowStartVenue(link)
            
            // 尝试从HTML的title标签中提取标题
            val titleRegex = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
            val titleMatch = titleRegex.find(htmlContent)
            val htmlTitle = titleMatch?.groupValues?.get(1)?.replace(" - 秀动ShowStart", "")?.trim()
            
            // 尝试从HTML中提取其他可能的信息
            // 查找可能包含演出名称的script标签
            val scriptRegex = Regex("<script>([\\s\\S]*?)</script>", RegexOption.IGNORE_CASE)
            val scriptMatches = scriptRegex.findAll(htmlContent)
            
            var extractedTitle: String? = null
            var extractedVenue: String? = null
            
            for (scriptMatch in scriptMatches) {
                val scriptContent = scriptMatch.groupValues[1]
                // 查找可能的演出信息
                val activityNameRegex = Regex("\"activityName\"\\s*:\\s*\"([^\"]+)\"")
                val siteNameRegex = Regex("\"siteName\"\\s*:\\s*\"([^\"]+)\"")
                
                val activityNameMatch = activityNameRegex.find(scriptContent)
                val siteNameMatch = siteNameRegex.find(scriptContent)
                
                if (activityNameMatch != null && extractedTitle == null) {
                    extractedTitle = activityNameMatch.groupValues[1]
                    Log.d(TAG, "Extracted title from script: $extractedTitle")
                }
                
                if (siteNameMatch != null && extractedVenue == null) {
                    extractedVenue = siteNameMatch.groupValues[1]
                    Log.d(TAG, "Extracted venue from script: $extractedVenue")
                }
            }
            
            // 如果从script中没有提取到信息，尝试其他方式
            if (extractedTitle == null) {
                // 查找可能包含演出信息的JSON数据
                val jsonRegex = Regex("\\{[^\\{\\}]*\"activityName\"[^\\{\\}]*\\}")
                val jsonMatch = jsonRegex.find(htmlContent)
                if (jsonMatch != null) {
                    val jsonString = jsonMatch.value
                    Log.d(TAG, "Found potential JSON data: $jsonString")
                    
                    // 简单解析JSON字符串
                    val activityNameJsonRegex = Regex("\"activityName\"\\s*:\\s*\"([^\"]+)\"")
                    val activityNameJsonMatch = activityNameJsonRegex.find(jsonString)
                    if (activityNameJsonMatch != null) {
                        extractedTitle = activityNameJsonMatch.groupValues[1]
                        Log.d(TAG, "Extracted title from JSON: $extractedTitle")
                    }
                    
                    val siteNameJsonRegex = Regex("\"siteName\"\\s*:\\s*\"([^\"]+)\"")
                    val siteNameJsonMatch = siteNameJsonRegex.find(jsonString)
                    if (siteNameJsonMatch != null) {
                        extractedVenue = siteNameJsonMatch.groupValues[1]
                        Log.d(TAG, "Extracted venue from JSON: $extractedVenue")
                    }
                }
            }
            
            return Concert(
                id = 0,
                title = linkTitle ?: extractedTitle ?: htmlTitle ?: "秀动演出 - 活动ID: $activityId",
                venue = linkVenue ?: extractedVenue ?: "未知场地",
                date = Date(),
                notes = "从秀动链接解析的演出信息\n链接: $link\n活动ID: $activityId",
                posterResId = 0,
                posterPath = "",
                ticketPrice = "",
                ticketPriceCurrency = "CNY",
                actualPaid = "",
                actualPaidCurrency = "CNY",
                otherFees = "",
                otherFeesCurrency = "CNY",
                performers = listOf(linkTitle ?: extractedTitle ?: htmlTitle ?: "未知演出者"),
                guests = emptyList(),
                status = "待看",
                category = "演唱会",
                rating = 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "ShowStart HTML content parsing failed", e)
            return parseShowStartFromText(link, activityId)
        }
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
     * 根据秀动API响应创建演出对象
     * @param response API响应
     * @param link 原始链接
     * @return 演出对象
     */
    private fun createConcertFromShowStartResponse(response: ShowStartActivityResponse, link: String): Concert {
        // 解析日期时间
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = try {
            dateFormat.parse(response.startTime) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        
        return Concert(
            id = 0,
            title = response.title,
            venue = response.venue,
            date = date,
            notes = "从秀动链接解析的演出信息\n链接: $link\n活动ID: ${response.id}\n描述: ${response.description}",
            posterResId = 0,
            posterPath = "",
            ticketPrice = "",
            ticketPriceCurrency = "CNY",
            actualPaid = "",
            actualPaidCurrency = "CNY",
            otherFees = "",
            otherFeesCurrency = "CNY",
            performers = response.performers,
            guests = emptyList(),
            status = "待看",
            category = "演唱会",
            rating = 0
        )
    }
    
    /**
     * 从秀动链接中提取活动ID
     * @param link 秀动链接
     * @return 活动ID，如果无法提取则返回null
     */
    private fun extractShowStartActivityId(link: String): String? {
        // 匹配 activityId 参数
        val activityIdRegex = Regex("activityId=(\\d+)")
        val activityIdMatch = activityIdRegex.find(link)
        if (activityIdMatch != null) {
            return activityIdMatch.groupValues[1]
        }
        
        // 匹配 /event/ 后面的数字ID
        val eventIdRegex = Regex("/event/(\\d+)")
        val eventIdMatch = eventIdRegex.find(link)
        if (eventIdMatch != null) {
            return eventIdMatch.groupValues[1]
        }
        
        return null
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
     * 创建秀动演出的模拟数据
     * @param activityId 活动ID
     * @param link 原始链接
     * @return 模拟的演出信息
     */
    private fun createMockShowStartConcert(activityId: String, link: String): Concert {
        // 解析链接中的演出信息
        val title = extractShowStartTitle(link) ?: "秀动演出 - 活动ID: $activityId"
        val venue = extractShowStartVenue(link) ?: "未知场地"
        val date = Date()
        
        return Concert(
            id = 0,
            title = title,
            venue = venue,
            date = date,
            notes = "从秀动链接解析的演出信息\n链接: $link\n活动ID: $activityId",
            posterResId = 0,
            posterPath = "",
            ticketPrice = "",
            ticketPriceCurrency = "CNY",
            actualPaid = "",
            actualPaidCurrency = "CNY",
            otherFees = "",
            otherFeesCurrency = "CNY",
            performers = if (title != "秀动演出 - 活动ID: $activityId") listOf(title) else listOf("未知演出者"),
            guests = emptyList(),
            status = "待看",
            category = "演唱会",
            rating = 0
        )
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
     * 从秀动链接文本中提取演出信息（回退方案）
     * @param link 链接文本
     * @param activityId 活动ID
     * @return 演出信息
     */
    private fun parseShowStartFromText(link: String, activityId: String): Concert? {
        // 从链接中提取演出信息
        val title = extractShowStartTitle(link)
        val venue = extractShowStartVenue(link)
        // 使用默认日期
        val date = Date()
        
        // 生成演出信息
        return Concert(
            id = 0,
            title = title ?: "秀动演出 - 活动ID: $activityId",
            venue = venue ?: "未知场地",
            date = date,
            notes = "从秀动链接解析的演出信息\n链接: $link\n活动ID: $activityId",
            posterResId = 0,
            posterPath = "",
            ticketPrice = "",
            ticketPriceCurrency = "CNY",
            actualPaid = "",
            actualPaidCurrency = "CNY",
            otherFees = "",
            otherFeesCurrency = "CNY",
            performers = if (title != null) listOf(title) else listOf("未知演出者"),
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
    
    /**
     * 从秀动链接文本中提取演出标题
     * @param text 包含链接的完整文本
     * @return 演出标题，如果无法提取则返回null
     */
    private fun extractShowStartTitle(text: String): String? {
        // 匹配【】中的演出名称，例如：【王以太「Love Me Later」2025巡回演唱会 深圳站】
        val titleRegex = Regex("【([^【】]+)】")
        val titleMatch = titleRegex.find(text)
        if (titleMatch != null) {
            return titleMatch.groupValues[1]
                .replace("「.*?」".toRegex(), "") // 移除书名号中的内容
                .trim()
        }
        
        return null
    }
    
    /**
     * 从秀动链接文本中提取演出场地
     * @param text 包含链接的完整文本
     * @return 演出场地，如果无法提取则返回null
     */
    private fun extractShowStartVenue(text: String): String? {
        // 匹配站点信息，例如：xxx站
        val venueRegex = Regex("(.+?)站")
        val venueMatch = venueRegex.find(text)
        if (venueMatch != null) {
            return "${venueMatch.groupValues[1]}站"
        }
        
        return null
    }
}