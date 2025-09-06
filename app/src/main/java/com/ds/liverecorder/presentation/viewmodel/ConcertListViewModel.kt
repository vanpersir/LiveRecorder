package com.ds.liverecorder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.usecase.ConcertUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 演出列表ViewModel，专注于UI状态管理
 */
class ConcertListViewModel(
    private val concertUseCases: ConcertUseCases
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConcertListUiState())
    val uiState: StateFlow<ConcertListUiState> = _uiState
    
    init {
        loadConcerts()
    }
    
    fun loadConcerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            concertUseCases.getAllConcerts()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
                .collectLatest { concerts ->
                    _uiState.value = _uiState.value.copy(
                        concerts = concerts,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    fun loadUpcomingConcerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            concertUseCases.getUpcomingConcerts()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
                .collectLatest { concerts ->
                    _uiState.value = _uiState.value.copy(
                        concerts = concerts,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    fun searchConcerts(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            if (query.isEmpty()) {
                loadConcerts()
            } else {
                concertUseCases.searchConcerts(query)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                    .collectLatest { concerts ->
                        _uiState.value = _uiState.value.copy(
                            concerts = concerts,
                            isLoading = false,
                            error = null
                        )
                    }
            }
        }
    }
}

/**
 * 演出列表UI状态
 */
data class ConcertListUiState(
    val concerts: List<Concert> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)