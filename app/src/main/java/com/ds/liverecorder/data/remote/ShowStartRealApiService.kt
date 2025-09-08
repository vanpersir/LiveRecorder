package com.ds.liverecorder.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * 秀动真实API服务接口
 * 用于获取秀动平台的演出信息
 * 根据网络资料，秀动的真实API可能在pro2-api.showstart.com域名下
 */
interface ShowStartRealApiService {
    
    /**
     * 根据活动ID获取演出详情
     * @param activityId 活动ID
     * @param appVersion App版本
     * @param terminal 终端类型
     * @param cua 鉴权参数
     * @param cusystime 系统时间
     * @param cuuserref 用户引用
     * @param cusut 用户会话令牌
     * @return 演出详情响应
     */
    @GET("pages/activity/detail")
    fun getActivityDetail(
        @Query("activityId") activityId: String,
        @Header("version") appVersion: String = "5.0.1",
        @Header("terminal") terminal: String = "android",
        @Header("cua") cua: String = "axcHZeQPfJz8guxJHFDgkOVRwBM/ZuWdwcRy0yHO0b0=",
        @Header("cusystime") cusystime: String = System.currentTimeMillis().toString(),
        @Header("cuuserref") cuuserref: String = "b3aeb6664c498e43df6e30f48999989c",
        @Header("cusut") cusut: String = ""
    ): Call<ResponseBody>
}