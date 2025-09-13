package com.ds.liverecorder.presentation.common.provider

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ds.liverecorder.data.database.ConcertDatabase
import com.ds.liverecorder.data.repository.ConcertLinkParserRepositoryImpl
import com.ds.liverecorder.data.repository.ConcertRepositoryImpl
import com.ds.liverecorder.data.repository.ExchangeRateRepositoryImpl
import com.ds.liverecorder.domain.repository.ConcertLinkParserRepository
import com.ds.liverecorder.domain.repository.ConcertRepository
import com.ds.liverecorder.domain.repository.ExchangeRateRepository
import com.ds.liverecorder.presentation.viewmodel.ConvertViewModelFactory

@Composable
inline fun <reified VM : ViewModel> concertViewModel(): VM {
    val activity = LocalContext.current as? ComponentActivity
        ?: error("LocalContext must be a ComponentActivity")
    
    val database = ConcertDatabase.getDatabase(activity)
    val concertDao = database.concertDao()
    val repository: ConcertRepository = ConcertRepositoryImpl(concertDao)
    val linkParserRepository: ConcertLinkParserRepository = ConcertLinkParserRepositoryImpl(activity)
    val exchangeRateRepository: ExchangeRateRepository = ExchangeRateRepositoryImpl()
    
    val factory = ConvertViewModelFactory(repository, linkParserRepository, exchangeRateRepository)
    
    return viewModel(viewModelStoreOwner = activity, factory = factory)
}