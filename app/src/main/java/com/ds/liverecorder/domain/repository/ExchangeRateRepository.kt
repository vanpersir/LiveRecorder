package com.ds.liverecorder.domain.repository

/**
 * 汇率仓库接口
 */
interface ExchangeRateRepository {
    /**
     * 获取最新汇率数据
     * @param base 基准货币代码
     * @param symbols 需要获取的货币代码，多个用逗号分隔
     * @return 汇率数据映射
     */
    suspend fun getLatestRates(base: String, symbols: String?): Map<String, Double>
}