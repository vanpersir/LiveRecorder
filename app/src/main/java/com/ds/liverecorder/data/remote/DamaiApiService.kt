package com.ds.liverecorder.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 大麦API服务接口
 * 用于获取大麦平台的演出信息
 */
interface DamaiApiService {
    
    /**
     * 根据项目ID获取演出详情
     * @param itemId 项目ID
     * @return 演出详情响应
     */
    @GET("detail/item")
    fun getItemDetail(
        @Query("itemId") itemId: String
    ): Call<DamaiItemResponse>
}

/**
 * 大麦项目响应数据类
 * @property id 项目ID
 * @property title 项目标题
 * @property venue 项目场地
 * @property showTime 项目演出时间
 * @property description 项目描述
 * @property performers 演出者列表
 */
data class DamaiItemResponse(
    val id: String,
    val title: String,
    val venue: String,
    val showTime: String, // 时间格式: "2025-01-01 19:30:00"
    val description: String,
    val performers: List<String>
)