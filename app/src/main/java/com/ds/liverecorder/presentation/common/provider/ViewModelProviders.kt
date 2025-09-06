package com.ds.liverecorder.presentation.common.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ds.liverecorder.data.database.ConcertDatabase
import com.ds.liverecorder.domain.repository.ConcertRepository
import com.ds.liverecorder.data.repository.ConcertRepositoryImpl
import com.ds.liverecorder.presentation.viewmodel.ConvertViewModelFactory

@Composable
inline fun <reified VM : ViewModel> concertViewModel(): VM {
    val context = LocalContext.current
    val database = ConcertDatabase.getDatabase(context)
    val concertDao = database.concertDao()
    val repository: ConcertRepository = ConcertRepositoryImpl(concertDao)
    
    val factory = ConvertViewModelFactory(repository)
    
    return viewModel(factory = factory)
}