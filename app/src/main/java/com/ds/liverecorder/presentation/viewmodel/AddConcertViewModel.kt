package com.ds.liverecorder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.usecase.ConcertUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 添加演出ViewModel，专注于UI状态管理
 */
class AddConcertViewModel(
    private val concertUseCases: ConcertUseCases
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddConcertUiState())
    val uiState: StateFlow<AddConcertUiState> = _uiState
    
    fun addConcert(concert: Concert) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = concertUseCases.addConcert(concert)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
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
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
}

/**
 * 添加演出UI状态
 */
data class AddConcertUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)