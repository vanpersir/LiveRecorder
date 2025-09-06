package com.ds.liverecorder.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 汇率API服务接口
 * 使用exchangerate-api.com提供的免费API
 */
interface ExchangeRateApiService {
    
    /**
     * 获取最新汇率数据
     * @param base 基准货币代码
     * @return 汇率数据响应
     */
    @GET("v6/latest/{base}")
    fun getLatestRates(
        @Path("base") base: String
    ): Call<ExchangeRateResponse>
}

/**
 * 汇率响应数据类
 * @property result 请求结果(success或error)
 * @property provider 提供者信息
 * @property documentation 文档链接
 * @property termsOfUse 使用条款链接
 * @property timeLastUpdateUnix 上次更新时间(unix时间戳)
 * @property timeLastUpdateUtc 上次更新时间(UTC格式)
 * @property timeNextUpdateUnix 下次更新时间(unix时间戳)
 * @property timeNextUpdateUtc 下次更新时间(UTC格式)
 * @property baseCode 基准货币代码
 * @property rates 汇率数据
 */
data class ExchangeRateResponse(
    val result: String,
    val provider: String,
    val documentation: String,
    val termsOfUse: String,
    val timeLastUpdateUnix: Long,
    val timeLastUpdateUtc: String,
    val timeNextUpdateUnix: Long,
    val timeNextUpdateUtc: String,
    val baseCode: String,
    val rates: Map<String, Double>
) {
    // 兼容旧的success属性
    val success: Boolean
        get() = result == "success"
}