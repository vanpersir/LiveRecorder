package com.ds.liverecorder.domain.usecase

import com.ds.liverecorder.domain.repository.ExchangeRateRepository

/**
 * 汇率转换用例
 * 负责处理汇率转换相关的业务逻辑
 */
class ExchangeRateUseCase(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    
    /**
     * 获取汇率并转换金额
     * @param amount 金额
     * @param fromCurrency 源货币
     * @param toCurrency 目标货币
     * @return 转换后的金额，如果无法转换则返回null
     */
    suspend fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double? {
        // 如果源货币和目标货币相同，直接返回原金额
        if (fromCurrency == toCurrency) {
            return amount
        }
        
        try {
            // 获取汇率数据
            val rates = exchangeRateRepository.getLatestRates(fromCurrency, null)
            
            // 获取目标货币的汇率
            val rate = rates[toCurrency]
            
            // 如果找到汇率，进行转换
            return if (rate != null) {
                amount * rate
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 获取支持的货币列表
     * @return 支持的货币代码列表
     */
    fun getSupportedCurrencies(): List<String> {
        return listOf(
            "CNY", "USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "HKD", "SGD"
        )
    }
}