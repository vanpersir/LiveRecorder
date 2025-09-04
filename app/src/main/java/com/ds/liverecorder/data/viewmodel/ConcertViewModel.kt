package com.ds.liverecorder.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.repository.ConcertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

class ConcertViewModel(private val repository: ConcertRepository) : ViewModel() {
    val allConcerts: LiveData<List<ConcertEntity>> = repository.allConcerts.asLiveData()
    val allPerformersNames: LiveData<List<String>> = repository.allPerformersNames.asLiveData()

    fun getAllConcertsFlow(): Flow<List<ConcertEntity>> {
        return repository.allConcerts
    }
    
    fun getConcertById(id: Long): Flow<ConcertEntity?> {
        return repository.getConcertByIdAsFlow(id)
    }

    fun getConcertsByPerformerName(performerName: String): Flow<List<ConcertEntity>> {
        return repository.getConcertsByPerformerName(performerName)
    }
    
    fun getUpcomingConcerts(): Flow<List<ConcertEntity>> {
        return repository.getConcertsAfterDate(Date())
    }

    fun insertConcert(concert: ConcertEntity) {
        viewModelScope.launch {
            repository.insertConcert(concert)
        }
    }

    fun updateConcert(concert: ConcertEntity) {
        viewModelScope.launch {
            repository.updateConcert(concert)
        }
    }

    fun deleteConcert(concert: ConcertEntity) {
        viewModelScope.launch {
            repository.deleteConcert(concert)
        }
    }

    fun deleteConcertById(id: Long) {
        viewModelScope.launch {
            repository.deleteConcertById(id)
        }
    }
}