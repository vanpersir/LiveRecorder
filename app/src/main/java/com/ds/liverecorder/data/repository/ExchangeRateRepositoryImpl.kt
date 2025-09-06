package com.ds.liverecorder.data.repository

import android.util.Log
import com.ds.liverecorder.data.remote.ExchangeRateApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import com.ds.liverecorder.domain.repository.ExchangeRateRepository as DomainExchangeRateRepository

/**
 * 汇率仓库实现类
 */
class ExchangeRateRepositoryImpl : DomainExchangeRateRepository {
    
    private val apiService: ExchangeRateApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApiService::class.java)
    }
    
    // 缓存数据结构：key为基准货币代码，value为缓存的汇率数据和过期时间
    private val cache = ConcurrentHashMap<String, Pair<Map<String, Double>, Long>>()
    
    /**
     * 获取最新汇率数据
     * @param base 基准货币代码
     * @param symbols 需要获取的货币代码，多个用逗号分隔（此参数在新API中不使用）
     * @return 汇率数据映射
     */
    override suspend fun getLatestRates(base: String, symbols: String?): Map<String, Double> {
        // 检查缓存是否有效
        val cachedData = cache[base]
        if (cachedData != null && System.currentTimeMillis() < cachedData.second) {
            // 缓存有效，直接返回缓存数据
            return cachedData.first
        }
        
        // 缓存无效或不存在，从网络获取数据
        return try {
            // 在IO线程中执行网络请求
            val response = withContext(Dispatchers.IO) {
                try {
                    val apiResponse = apiService.getLatestRates(base)
                    apiResponse.execute()
                } catch (e: Exception) {
                    Log.e("ExchangeRateRepo", "Exception during API call", e)
                    null
                }
            }
            
            if (response != null && response.isSuccessful) {
                val body = response.body()
                
                if (body != null && body.success) {
                    // 计算缓存过期时间（第二天0点）
                    val expirationTime = getNextMidnight()
                    
                    // 缓存数据
                    cache[base] = Pair(body.rates, expirationTime)
                    
                    body.rates
                } else {
                    Log.d("ExchangeRateRepo", "API returned unsuccessful response or null body")
                    // 如果网络请求失败，检查是否有缓存数据可以使用
                    cachedData?.first ?: emptyMap()
                }
            } else {
                Log.e("ExchangeRateRepo", "API call failed. Response is null or not successful")
                // 如果网络请求失败，检查是否有缓存数据可以使用
                cachedData?.first ?: emptyMap()
            }
        } catch (e: Exception) {
            Log.e("ExchangeRateRepo", "Exception during API call", e)
            e.printStackTrace()
            // 如果网络请求失败，检查是否有缓存数据可以使用
            cachedData?.first ?: emptyMap()
        }
    }
    
    /**
     * 计算第二天0点的时间戳
     * @return 下一天0点的时间戳
     */
    private fun getNextMidnight(): Long {
        val calendar = Calendar.getInstance()
        // 设置为下一天的0点
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ExchangeRateRepositoryImpl? = null
        
        fun getInstance(): ExchangeRateRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExchangeRateRepositoryImpl().also { INSTANCE = it }
            }
        }
    }
}