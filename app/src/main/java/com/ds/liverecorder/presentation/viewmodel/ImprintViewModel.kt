package com.ds.liverecorder.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.usecase.ConcertUseCases
import com.ds.liverecorder.domain.usecase.ExchangeRateUseCase
import com.ds.liverecorder.presentation.feature.imprint.screen.ImprintDisplayMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 印记页面ViewModel
 * 负责管理印记页面的状态和业务逻辑
 */
class ImprintViewModel(
    private val concertUseCases: ConcertUseCases,
    private val exchangeRateUseCase: ExchangeRateUseCase
) : ViewModel() {
    
    // UI状态
    var uiState by mutableStateOf(ImprintUiState())
        private set

    private val _displayMode = MutableStateFlow(ImprintDisplayMode.STATISTICS) // 演出记录默认为卡片视图
    val displayMode: StateFlow<ImprintDisplayMode> = _displayMode
    
    // 获取所有演出数据
    val concerts: StateFlow<List<Concert>> = concertUseCases.getAllConcerts()
        .map { concerts -> concerts.sortedByDescending { it.date } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadConcerts()
        loadExchangeRates()
    }
    
    /**
     * 更新显示模式
     */
    fun updateDisplayMode(mode: ImprintDisplayMode) {
        _displayMode.value = mode
    }
    
    /**
     * 加载演出数据
     */
    private fun loadConcerts() {
        viewModelScope.launch {
            try {
                // 数据已经通过StateFlow自动更新
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 加载汇率数据
     */
    private fun loadExchangeRates() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoadingRates = true)
                // 汇率数据通过ExchangeRateUseCase自动处理
                uiState = uiState.copy(isLoadingRates = false)
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = uiState.copy(isLoadingRates = false)
            }
        }
    }
    
    /**
     * 更新选中的货币
     */
    fun updateSelectedCurrency(currency: String) {
        uiState = uiState.copy(selectedCurrency = currency)
        // 当货币改变时重新计算总花费
        calculateTotalSpent()
    }
    
    /**
     * 计算总花费并转换为统一货币单位
     */
    private fun calculateTotalSpent() {
        viewModelScope.launch {
            try {
                val watchedConcerts = concerts.value.filter { 
                    it.status == "正常"
                }
                
                val totalSpent = watchedConcerts.sumOf { concert ->
                    val amount = concert.actualPaid.toDoubleOrNull() ?: 0.0
                    val sourceCurrency = concert.actualPaidCurrency.uppercase()
                    val targetCurrency = uiState.selectedCurrency

                    // 如果源货币和目标货币相同，直接返回
                    if (sourceCurrency == targetCurrency) {
                        amount
                    } else {
                        // 尝试使用汇率转换
                        val convertedAmount = exchangeRateUseCase.convertCurrency(
                            amount = amount,
                            fromCurrency = sourceCurrency,
                            toCurrency = targetCurrency
                        )

                        convertedAmount ?: amount // 如果转换失败，使用原始金额
                    }
                }
                
                uiState = uiState.copy(totalSpent = totalSpent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 按年份统计已观看的演出
     */
    fun getConcertsByYear(): List<Pair<String, Int>> {
        return concerts.value
            .filter { 
                it.status == "正常" && it.date.before(Date()) 
            }
            .groupBy { 
                java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(it.date) 
            }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.first }
    }
    
    /**
     * 按分类统计已观看的演出
     */
    fun getConcertsByCategory(): List<Pair<String, Int>> {
        return concerts.value
            .filter { 
                it.status == "正常" && it.date.before(Date()) 
            }
            .groupBy { it.category }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }
}

/**
 * 印记页面UI状态
 */
data class ImprintUiState(
    val isLoadingRates: Boolean = false,
    val selectedCurrency: String = "CNY",
    val totalSpent: Double = 0.0
)