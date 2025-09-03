package com.ds.liverecorder.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ds.liverecorder.data.repository.ConcertRepository

class ConcertViewModelFactory(private val repository: ConcertRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConcertViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConcertViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}