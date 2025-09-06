package com.ds.liverecorder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.usecase.ConcertUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 演出详情ViewModel，专注于UI状态管理
 */
class ConcertDetailViewModel(
    private val concertUseCases: ConcertUseCases
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConcertDetailUiState())
    val uiState: StateFlow<ConcertDetailUiState> = _uiState
    
    fun loadConcert(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = concertUseCases.getConcertById(id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    concert = result.getOrNull(),
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun updateConcert(concert: Concert) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = concertUseCases.updateConcert(concert)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    concert = concert,
                    isLoading = false,
                    error = null,
                    isSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message,
                    isSuccess = false
                )
            }
        }
    }
    
    fun deleteConcert(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = concertUseCases.deleteConcert(id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    concert = null,
                    isLoading = false,
                    error = null,
                    isSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message,
                    isSuccess = false
                )
            }
        }
    }
}

/**
 * 演出详情UI状态
 */
data class ConcertDetailUiState(
    val concert: Concert? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSuccess: Boolean = false
)