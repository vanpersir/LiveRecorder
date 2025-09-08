package com.ds.liverecorder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ds.liverecorder.domain.repository.ConcertLinkParserRepository
import com.ds.liverecorder.domain.repository.ConcertRepository
import com.ds.liverecorder.domain.repository.ExchangeRateRepository
import com.ds.liverecorder.domain.usecase.ConcertUseCases
import com.ds.liverecorder.domain.usecase.ExchangeRateUseCase
import com.ds.liverecorder.domain.usecase.ParseConcertLinkUseCase

/**
 * ViewModel工厂类
 */
class ConvertViewModelFactory(
    concertRepository: ConcertRepository,
    private val concertLinkParserRepository: ConcertLinkParserRepository,
    exchangeRateRepository: ExchangeRateRepository
) : ViewModelProvider.Factory {
    
    // 创建统一的业务逻辑入口
    private val concertUseCases = ConcertUseCases(concertRepository)
    private val parseConcertLinkUseCase = ParseConcertLinkUseCase(concertLinkParserRepository)
    private val exchangeRateUseCase = ExchangeRateUseCase(exchangeRateRepository)
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ConcertListViewModel::class.java) -> {
                ConcertListViewModel(
                    concertUseCases
                ) as T
            }
            modelClass.isAssignableFrom(ConcertDetailViewModel::class.java) -> {
                ConcertDetailViewModel(
                    concertUseCases
                ) as T
            }
            modelClass.isAssignableFrom(AddConcertViewModel::class.java) -> {
                AddConcertViewModel(
                    concertUseCases,
                    parseConcertLinkUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}