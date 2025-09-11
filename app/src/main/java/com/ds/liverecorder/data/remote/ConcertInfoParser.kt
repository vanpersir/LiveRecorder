package com.ds.liverecorder.data.remote

import android.content.Context
import android.util.Log
import com.ds.liverecorder.domain.model.Concert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 演出信息解析器
 */
object ConcertInfoParser {
    private const val TAG = "ConcertInfoParser"
    
    /**
     * 从JSON响应中解析演出信息
     *
     * @param jsonString JSON格式的响应数据
     * @return 解析出的Concert对象，如果解析失败则返回null
     */
    fun parseConcertFromJson(jsonString: String, posterPath: String?): Concert? {
        return try {
            val jsonObject = JSONObject(jsonString)

            // 检查响应状态
            val status = jsonObject.optInt("status", -1)
            val state = jsonObject.optString("state", "")

            if (status != 200 && state != "1") {
                Log.e(TAG, "Invalid response status: status=$status, state=$state")
                return null
            }

            // 获取result对象
            val result = jsonObject.optJSONObject("result") ?: run {
                Log.e(TAG, "Missing result object in response")
                return null
            }

            // 提取基本信息
            val activityId = result.optLong("activityId")
            val activityName = result.optString("activityName", "")
            val price = result.optString("price", "")

            // 提取演出时间
            val showTime = result.optString("showTime", "")
            val showStartTime = result.optLong("showStartTime", 0)

            // 提取地点信息
            val site = result.optJSONObject("site") ?: JSONObject()
            val siteName = site.optString("name", "")
            val siteAddress = site.optString("address", "")
            val venue = siteName.ifEmpty { siteAddress }

            // 提取表演者信息
            val performers = mutableListOf<String>()
            val guests = mutableListOf<String>()
            val sessionUserInfos = result.optJSONArray("sessionUserInfos")
            if (sessionUserInfos != null && sessionUserInfos.length() > 0) {
                for (i in 0 until sessionUserInfos.length()) {
                    val userInfos = sessionUserInfos.getJSONObject(i).optJSONArray("userInfos")
                    if (userInfos != null) {
                        for (j in 0 until userInfos.length()) {
                            val userInfo = userInfos.getJSONObject(j)
                            val roleType = userInfo.optInt("roleType", 1)
                            val performerName = userInfo.optString("name", "")
                            if (performerName.isNotEmpty()) {
                                if (roleType == 1) {
                                    performers.add(performerName)
                                } else if (roleType == 2) {
                                    guests.add(performerName)
                                }
                            }
                        }
                    }
                }
            }

            // 构造标题
            val title = activityName.ifEmpty {
                "${performers.joinToString(", ")} 演唱会"
            }

            // 构造日期
            val date = if (showStartTime > 0) {
                Date(showStartTime)
            } else {
                // 尝试从showTime字符串解析日期
                parseDateFromString(showTime)
            }

            // 创建Concert对象
            Concert(
                id = activityId,
                title = title,
                venue = venue,
                date = date,
                notes = "",
                posterResId = 0,
                posterPath = posterPath ?: "", // 初始为网络URL，如果为null则设为空字符串
                ticketPrice = price,
                ticketPriceCurrency = "CNY",
                actualPaid = "",
                actualPaidCurrency = "CNY",
                otherFees = "",
                otherFeesCurrency = "CNY",
                performers = performers,
                guests = guests,
                status = "正常",
                category = "现场",
                rating = 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing concert info from JSON", e)
            null
        }
    }

    /**
     * 解析演出信息并下载海报图片
     *
     * @param jsonString JSON格式的响应数据
     * @param context 应用上下文
     * @param onResult 回调函数，当解析完成和图片下载完成后调用
     */
    fun parseConcertFromJsonWithLocalPoster(
        jsonString: String,
        context: Context,
        onResult: (Concert?) -> Unit
    ) {
        try {
            // 先从JSON中获取海报URL
            val posterUrl = try {
                val jsonObject = JSONObject(jsonString)
                val result = jsonObject.optJSONObject("result")
                result?.optString("avatar", "") ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting poster URL from JSON", e)
                ""
            }

            if (posterUrl.isNotEmpty()) {
                // 启动协程下载海报图片
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val directory = File(context.filesDir, "posters")
                        val savedImagePath = ImageDownloader.downloadAndSaveImage(
                            posterUrl,
                            directory
                        )

                        // 使用本地保存的图片路径创建 Concert 对象，只创建一次
                        val concert = parseConcertFromJson(jsonString, posterPath = savedImagePath ?: posterUrl)

                        // 在主线程中回调结果
                        withContext(Dispatchers.Main) {
                            onResult(concert)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error downloading poster image", e)
                        // 出错时直接解析concert对象
                        val concert = parseConcertFromJson(jsonString, posterUrl)
                        withContext(Dispatchers.Main) {
                            onResult(concert)
                        }
                    }
                }
            } else {
                // 没有海报URL时直接解析concert对象
                val concert = parseConcertFromJson(jsonString, posterUrl)
                Log.d(TAG, "Created concert without poster URL, posterPath: ${concert?.posterPath}")
                onResult(concert)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing concert info", e)
            onResult(null)
        }
    }
    
    /**
     * 从字符串中解析日期
     */
    private fun parseDateFromString(dateString: String): Date {
        return try {
            // 尝试解析 "2025.09.25 周四 20:00" 格式
            val format = SimpleDateFormat("yyyy.MM.dd E HH:mm", Locale.getDefault())
            format.timeZone = TimeZone.getDefault() // 确保使用设备默认时区
            format.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date from string: $dateString", e)
            Date()
        }
    }
}