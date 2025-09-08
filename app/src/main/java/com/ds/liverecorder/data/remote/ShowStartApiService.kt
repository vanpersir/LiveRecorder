package com.ds.liverecorder.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 秀动API服务接口
 * 用于获取秀动平台的演出信息
 */
interface ShowStartApiService {
    
    /**
     * 根据活动ID获取演出详情
     * @param activityId 活动ID
     * @return 演出详情响应
     */
    @GET("v3/wap/activity/details")
    fun getActivityDetail(
        @Query("activityId") activityId: String
    ): Call<ResponseBody>
}

/**
 * 秀动活动响应数据类
 * @property id 活动ID
 * @property title 活动标题
 * @property venue 活动场地
 * @property startTime 活动开始时间
 * @property description 活动描述
 * @property performers 演出者列表
 */
data class ShowStartActivityResponse(
    val id: String,
    val title: String,
    val venue: String,
    val startTime: String, // 时间格式: "2025-01-01 19:30:00"
    val description: String,
    val performers: List<String>,
    // 添加更多可能的字段
    val address: String? = null,
    val city: String? = null,
    val siteName: String? = null, // 场地名称
    val notice: String? = null, // 购票须知
    val projectInfo: String? = null // 项目信息
)