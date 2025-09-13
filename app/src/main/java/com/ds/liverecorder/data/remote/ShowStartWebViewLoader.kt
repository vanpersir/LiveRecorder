package com.ds.liverecorder.data.remote

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ds.liverecorder.domain.model.Concert
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.util.regex.Pattern

/**
 * 秀动WebView加载器
 * 通过WebView加载秀动分享链接并解析演出信息
 */
class ShowStartWebViewLoader(private val context: Context) {
    
    companion object {
        private const val TAG = "ShowStartWebViewLoader"
        private const val TARGET_URL = "https://wap.showstart.com/v3/wap/activity/details"
    }
    
    /**
     * 通过WebView加载秀动链接并解析演出信息
     * @param link 秀动分享链接
     * @return 解析出的演出信息
     */
    suspend fun parseLinkWithWebView(link: String): Concert? {
        return kotlinx.coroutines.withContext(Dispatchers.Main) {
            val deferredConcert = CompletableDeferred<Concert?>()
            val webView = WebView(context)

            // 配置WebView
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
            }
            
            webView.visibility = View.GONE
            
            // 设置WebView客户端
            webView.webViewClient = object : WebViewClient() {
                private var interceptedRequest: WebResourceRequest? = null
                private var isProcessing = false // 添加标志防止重复处理

                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    val url = request.url.toString()
                    
                    // 检查是否是目标详情页请求，并且尚未处理过
                    if (url.contains(TARGET_URL) && !isProcessing) {
                        isProcessing = true // 标记为正在处理
                        interceptedRequest = request
                        
                        // 在后台线程获取并打印响应内容
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                fetchAndPrintResponseWithWebViewHeaders(url, request, deferredConcert)
                            } catch (e: Exception) {
                                deferredConcert.complete(null)
                            } finally {
                                // 延迟清理WebView，确保认证流程完成
                                Handler(Looper.getMainLooper()).postDelayed({
                                    try {
                                        webView.destroy()
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error destroying WebView", e)
                                    }
                                }, 3000) // 延迟3秒销毁，确保认证流程完成
                            }
                        }
                        
                        // 返回一个空响应以拦截请求，因为我们自己处理它
                        val response = WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                        return response
                    }

                    val response = super.shouldInterceptRequest(view, request)
                    return response
                }


                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (view != null) {
                        logWebViewDetails(view, "onPageStarted")
                    }
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    if (view != null) {
                        // 记录WebView详细信息
                        logWebViewDetails(view, "onPageFinished")
                    }
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    Log.e(TAG, "WebView error - Code: $errorCode, Description: $description, Url: $failingUrl")
                    if (view != null) {
                        logWebViewDetails(view, "onReceivedError")
                    }
                    if (!deferredConcert.isCompleted) {
                        deferredConcert.complete(null)
                    }
                    webView.destroy()
                }
                
                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    Log.e(TAG, "WebView HTTP error - Request: ${request?.url}, Response: ${errorResponse?.statusCode}")
                    Log.e(TAG, "WebView HTTP error headers: ${errorResponse?.responseHeaders}")
                    if (view != null) {
                        logWebViewDetails(view, "onReceivedHttpError")
                    }
                }
            }
            
            // 启用详细日志
            webView.webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    Log.d(TAG, "WebView console - ${consoleMessage?.messageLevel()}: ${consoleMessage?.message()} " +
                            "(${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                    return super.onConsoleMessage(consoleMessage)
                }
                
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Log.d(TAG, "WebView loading progress: $newProgress%")
                }
            }
            
            // 记录初始WebView详细信息
            logWebViewDetails(webView, "initial")
            
            // 加载链接
            Log.d(TAG, "Loading URL: $link")
            webView.loadUrl(link)
            
            try {
                deferredConcert.await()
            } catch (e: Exception) {
                Log.e(TAG, "Error while waiting for concert parsing", e)
                webView.destroy()
                null
            }
        }
    }

    /**
     * 记录WebView的详细信息
     */
    private fun logWebViewDetails(webView: WebView, event: String) {
        try {
            Log.d(TAG, "=== WebView Details ($event) ===")
            Log.d(TAG, "URL: ${webView.url}")
            Log.d(TAG, "Title: ${webView.title}")
            Log.d(TAG, "Progress: ${webView.progress}")
            Log.d(TAG, "Settings - JavaScript Enabled: ${webView.settings.javaScriptEnabled}")
            Log.d(TAG, "Settings - DOM Storage Enabled: ${webView.settings.domStorageEnabled}")
            Log.d(TAG, "Settings - User Agent: ${webView.settings.userAgentString}")
            Log.d(TAG, "Settings - Database Enabled: ${webView.settings.databaseEnabled}")
            Log.d(TAG, "Settings - Use Wide Viewport: ${webView.settings.useWideViewPort}")
            Log.d(TAG, "Settings - Load With Overview Mode: ${webView.settings.loadWithOverviewMode}")
            Log.d(TAG, "=== End WebView Details ($event) ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging WebView details for event: $event", e)
        }
    }
    
    /**
     * 使用WebView的请求头和负载获取并打印URL响应内容
     */
    private fun fetchAndPrintResponseWithWebViewHeaders(
        url: String, 
        request: WebResourceRequest,
        deferredConcert: CompletableDeferred<Concert?>
    ) {
        try {
            val sortedHeaders = request.requestHeaders?.toSortedMap()
            sortedHeaders?.forEach { (key, value) ->
                Log.d(TAG, "  $key: $value")
            }
            
            // 尝试捕获POST请求中的JSON数据
            var jsonData: String? = null
            if (request.method.equals("POST", ignoreCase = true)) {
                // 注意：由于WebView的限制，我们无法直接获取请求体内容
                // 但可以根据目标URL和常见的参数结构来构造可能的JSON数据
                try {
                    jsonData = constructPossibleJsonData(url, request.requestHeaders)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not construct possible JSON data", e)
                }
            }
            
            val client = OkHttpClient()
            
            // 构建请求，使用WebView的请求头
            val requestBuilder = Request.Builder()
                .url(url)
            
            // 添加WebView的请求头，并特别处理一些关键头部
            var hasContentType = false
            request.requestHeaders?.forEach { (key, value) ->
                // 避免添加可能引起问题的头部
                if (!key.equals("Accept-Encoding", ignoreCase = true)) {
                    requestBuilder.addHeader(key, value)
                    if (key.equals("Content-Type", ignoreCase = true)) {
                        hasContentType = true
                    }
                }
            }
            
            // 确保Content-Type头部存在
            if (!hasContentType) {
                requestBuilder.addHeader("Content-Type", "application/json")
            }
            
            // 添加priority头部信息（如果不存在）
            if (request.requestHeaders?.containsKey("priority") == false) {
                requestBuilder.addHeader("priority", "u=1, i")
            }
            
            // 如果有请求体，添加请求体
            var requestBodyString = ""
            if (request.method.equals("POST", ignoreCase = true) || 
                request.method.equals("PUT", ignoreCase = true)) {
                // 使用构造的JSON数据作为请求体
                requestBodyString = jsonData ?: ""
                val requestBody = requestBodyString.toRequestBody("application/json".toMediaType())
                requestBuilder.method(request.method, requestBody)
            } else {
                requestBuilder.method(request.method, null)
            }
            
            val httpRequest = requestBuilder.build()
            Log.d(TAG, "httpRequest: $httpRequest")
            
            val response = client.newCall(httpRequest).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    // 解析演出信息并下载海报图片
                    ConcertInfoParser.parseConcertFromJsonWithLocalPoster(responseBody, context) { concert ->
                        if (concert != null) {
                            Log.d(TAG, "Successfully parsed concert: ${concert.title}")
                            deferredConcert.complete(concert)
                        } else {
                            Log.e(TAG, "Failed to parse concert from response")
                            deferredConcert.complete(null)
                        }
                    }
                } else {
                    Log.d(TAG, "Response body is null")
                    deferredConcert.complete(null)
                }
            } else {
                Log.e(TAG, "Request failed with code: ${response.code}")
                Log.e(TAG, "Response message: ${response.message}")
                
                // 尝试读取错误响应体
                val errorBody = response.body?.string()
                if (errorBody != null) {
                    Log.e(TAG, "Error response body: $errorBody")
                }
                
                deferredConcert.complete(null)
            }
            
            response.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching response with WebView headers for URL: $url", e)
            deferredConcert.complete(null)
        }
    }

    /**
     * 根据URL构造可能的JSON数据
     */
    private fun constructPossibleJsonData(url: String, headers: Map<String, String>? = null): String? {
        try {
            // 从headers中获取st_flpv
            val stFlpv = headers?.get("st_flpv") ?: ""
            
            // 尝试从headers中的Referer获取activityId
            var activityId: String? = null
            if (headers != null) {
                val referer = headers["Referer"]
                if (referer != null) {
                    val activityIdPattern = Pattern.compile("[?&]activityId=(\\d+)")
                    val activityIdMatcher = activityIdPattern.matcher(referer)
                    if (activityIdMatcher.find()) {
                        activityId = activityIdMatcher.group(1)
                    }
                }
            }
            
            // 如果从Referer中没有获取到activityId，则从URL中获取
            if (activityId == null) {
                val activityIdPattern = Pattern.compile("[?&]activityId=(\\d+)")
                val activityIdMatcher = activityIdPattern.matcher(url)
                if (activityIdMatcher.find()) {
                    activityId = activityIdMatcher.group(1)
                }
            }
            
            // 只有当activityId存在时才构造JSON
            if (activityId != null) {
                // 构造与curl命令完全一致的JSON数据
                val json = """{"activityId":"$activityId","coupon":"","shareId":"","previewPwd":"","st_flpv":"$stFlpv","sign":"","trackPath":""}"""
                return json
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error constructing possible JSON data from URL: $url", e)
        }
        
        return null
    }

}