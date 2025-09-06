package com.ds.liverecorder.presentation.feature.add.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ds.liverecorder.presentation.viewmodel.ConcertDetailViewModel
import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.presentation.common.provider.concertViewModel

@Composable
fun EditConcertScreen(
    concertId: Long,
    viewModel: ConcertDetailViewModel = concertViewModel(),
    onBack: () -> Unit = {},
    onUpdate: (Concert) -> Unit = {}
) {
    // 获取演出数据
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.concert != null) {
        AddConcertScreen(
            onBack = onBack,
            onSave = onUpdate,
            initialConcert = uiState.concert
        )
    }
}