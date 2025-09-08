package com.ds.liverecorder.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.usecase.ConcertUseCases
import com.ds.liverecorder.domain.usecase.ParseConcertLinkUseCase
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 添加演出ViewModel
 * 负责管理添加/编辑演出界面的状态和业务逻辑
 */
class AddConcertViewModel(
    private val concertUseCases: ConcertUseCases,
    private val parseConcertLinkUseCase: ParseConcertLinkUseCase
) : ViewModel() {
    
    var uiState by mutableStateOf(AddConcertUiState())
        private set
    
    /**
     * 设置初始演出信息（用于编辑模式）
     */
    fun setInitialConcert(concert: Concert?) {
        uiState = uiState.copy(
            id = concert?.id ?: 0L,
            title = concert?.title ?: "",
            venue = concert?.venue ?: "",
            date = concert?.date ?: Date(),
            notes = concert?.notes ?: "",
            posterPath = concert?.posterPath ?: "",
            ticketPrice = concert?.ticketPrice ?: "",
            ticketPriceCurrency = concert?.ticketPriceCurrency ?: "CNY",
            actualPaid = concert?.actualPaid ?: "",
            actualPaidCurrency = concert?.actualPaidCurrency ?: "CNY",
            otherFees = concert?.otherFees ?: "",
            otherFeesCurrency = concert?.otherFeesCurrency ?: "CNY",
            performers = concert?.performers?.joinToString(", ") ?: "",
            guests = concert?.guests?.joinToString(", ") ?: "",
            status = concert?.status ?: "",
            category = concert?.category ?: "",
            rating = concert?.rating ?: 0,
            isEditMode = concert != null
        )
    }
    
    /**
     * 更新标题
     */
    fun updateTitle(title: String) {
        uiState = uiState.copy(title = title)
    }
    
    /**
     * 更新场地
     */
    fun updateVenue(venue: String) {
        uiState = uiState.copy(venue = venue)
    }
    
    /**
     * 更新日期
     */
    fun updateDate(date: Date) {
        uiState = uiState.copy(date = date)
    }
    
    /**
     * 更新备注
     */
    fun updateNotes(notes: String) {
        uiState = uiState.copy(notes = notes)
    }
    
    /**
     * 更新海报路径
     */
    fun updatePosterPath(posterPath: String) {
        uiState = uiState.copy(posterPath = posterPath)
    }
    
    /**
     * 更新票价
     */
    fun updateTicketPrice(ticketPrice: String) {
        uiState = uiState.copy(ticketPrice = ticketPrice)
    }
    
    /**
     * 更新票价货币
     */
    fun updateTicketPriceCurrency(currency: String) {
        uiState = uiState.copy(ticketPriceCurrency = currency)
    }
    
    /**
     * 更新实付金额
     */
    fun updateActualPaid(actualPaid: String) {
        uiState = uiState.copy(actualPaid = actualPaid)
    }
    
    /**
     * 更新实付金额货币
     */
    fun updateActualPaidCurrency(currency: String) {
        uiState = uiState.copy(actualPaidCurrency = currency)
    }
    
    /**
     * 更新其他费用
     */
    fun updateOtherFees(otherFees: String) {
        uiState = uiState.copy(otherFees = otherFees)
    }
    
    /**
     * 更新其他费用货币
     */
    fun updateOtherFeesCurrency(currency: String) {
        uiState = uiState.copy(otherFeesCurrency = currency)
    }
    
    /**
     * 更新演出者
     */
    fun updatePerformers(performers: String) {
        uiState = uiState.copy(performers = performers)
    }
    
    /**
     * 更新嘉宾
     */
    fun updateGuests(guests: String) {
        uiState = uiState.copy(guests = guests)
    }
    
    /**
     * 更新状态
     */
    fun updateStatus(status: String) {
        uiState = uiState.copy(status = status)
    }
    
    /**
     * 更新分类
     */
    fun updateCategory(category: String) {
        uiState = uiState.copy(category = category)
    }
    
    /**
     * 更新评分
     */
    fun updateRating(rating: Int) {
        uiState = uiState.copy(rating = rating)
    }
    
    /**
     * 解析链接
     */
    fun parseLink(link: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isParsingLink = true, linkParseError = null)
            try {
                val concert = parseConcertLinkUseCase(link)
                if (concert != null) {
                    // 更新UI状态
                    uiState = uiState.copy(
                        title = concert.title,
                        venue = concert.venue,
                        date = concert.date,
                        notes = concert.notes,
                        ticketPrice = concert.ticketPrice,
                        ticketPriceCurrency = concert.ticketPriceCurrency,
                        actualPaid = concert.actualPaid,
                        actualPaidCurrency = concert.actualPaidCurrency,
                        otherFees = concert.otherFees,
                        otherFeesCurrency = concert.otherFeesCurrency,
                        performers = concert.performers.joinToString(", "),
                        guests = concert.guests.joinToString(", "),
                        status = concert.status,
                        category = concert.category,
                        rating = concert.rating,
                        isParsingLink = false
                    )
                } else {
                    uiState = uiState.copy(
                        isParsingLink = false,
                        linkParseError = "无法解析该链接"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isParsingLink = false,
                    linkParseError = "解析链接时发生错误: ${e.message}"
                )
            }
        }
    }

    /**
     * 保存演出
     */
    fun saveConcert(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val concert = Concert(
                    id = uiState.id,
                    title = uiState.title,
                    venue = uiState.venue,
                    date = uiState.date,
                    notes = uiState.notes,
                    posterResId = 0,
                    posterPath = uiState.posterPath,
                    ticketPrice = uiState.ticketPrice,
                    ticketPriceCurrency = uiState.ticketPriceCurrency,
                    actualPaid = uiState.actualPaid,
                    actualPaidCurrency = uiState.actualPaidCurrency,
                    otherFees = uiState.otherFees,
                    otherFeesCurrency = uiState.otherFeesCurrency,
                    performers = uiState.performers.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() },
                    guests = uiState.guests.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() },
                    status = uiState.status,
                    category = uiState.category,
                    rating = uiState.rating
                )
                
                if (uiState.isEditMode) {
                    concertUseCases.updateConcert(concert)
                } else {
                    concertUseCases.addConcert(concert)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "保存失败")
            }
        }
    }
}

/**
 * 添加演出界面状态数据类
 */
data class AddConcertUiState(
    val id: Long = 0L,
    val title: String = "",
    val venue: String = "",
    val date: Date = Date(),
    val notes: String = "",
    val posterPath: String = "",
    val ticketPrice: String = "",
    val ticketPriceCurrency: String = "CNY",
    val actualPaid: String = "",
    val actualPaidCurrency: String = "CNY",
    val otherFees: String = "",
    val otherFeesCurrency: String = "CNY",
    val performers: String = "",
    val guests: String = "",
    val status: String = "",
    val category: String = "",
    val rating: Int = 0,
    val isEditMode: Boolean = false,
    val isParsingLink: Boolean = false,
    val linkParseError: String? = null
)