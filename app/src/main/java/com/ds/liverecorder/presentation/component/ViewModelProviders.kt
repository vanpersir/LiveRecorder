package com.ds.liverecorder.presentation.component

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ds.liverecorder.data.ConcertDatabase
import com.ds.liverecorder.data.repository.ConcertRepository
import com.ds.liverecorder.data.viewmodel.ConcertViewModel

@Composable
inline fun <reified VM : ViewModel> concertViewModel(): VM {
    val context = LocalContext.current
    val database = ConcertDatabase.getDatabase(context)
    val concertDao = database.concertDao()
    val performerDao = database.performerDao()
    val repository = ConcertRepository(concertDao, performerDao)
    
    return viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ConcertViewModel(repository) as T
        }
    })
}