package com.ds.liverecorder.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.viewmodel.ConcertViewModel
import com.ds.liverecorder.presentation.component.concertViewModel

@Composable
fun EditConcertScreen(
    concertId: Long,
    viewModel: ConcertViewModel = concertViewModel(),
    onBack: () -> Unit = {},
    onUpdate: (ConcertEntity) -> Unit = {}
) {
    // 获取演出数据
    val concert by viewModel.getConcertById(concertId).collectAsState(initial = null)
    
    if (concert != null) {
        AddConcertScreen(
            onBack = onBack,
            onSave = onUpdate,
            initialConcert = concert
        )
    }
}