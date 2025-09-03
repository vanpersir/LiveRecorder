package com.ds.liverecorder.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ds.liverecorder.data.ConcertDatabase
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.entity.PerformerEntity
import com.ds.liverecorder.data.repository.ConcertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class ConcertViewModel(private val repository: ConcertRepository) : ViewModel() {
    val allConcerts: LiveData<List<ConcertEntity>> = repository.getAllConcerts().asLiveData()
    val allPerformersNames: LiveData<List<String>> = repository.getAllPerformersNames().asLiveData()

    fun getAllConcertsFlow(): Flow<List<ConcertEntity>> {
        return repository.getAllConcerts()
    }

    fun getConcertsByPerformerName(performerName: String): Flow<List<ConcertEntity>> {
        return repository.getConcertsByPerformerName(performerName)
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

    /**
     * 自动更新过期演出的状态
     * 将状态为"待看"且演出时间已过期的演出状态更新为"已看"
     */
    fun updateExpiredConcertsStatus() {
        viewModelScope.launch {
            try {
                // 获取所有演出（只获取一次）
                val concerts = repository.getAllConcerts().first()
                val now = Date()
                var updatedCount = 0
                concerts.forEach { concert ->
                    // 检查演出状态是否为"待看"且演出时间已过期
                    if (concert.status == "待看" && concert.date.before(now)) {
                        // 更新状态为"已看"
                        val updatedConcert = concert.copy(status = "已看")
                        repository.updateConcert(updatedConcert)
                        updatedCount++
                    }
                }
                // 可选：添加日志记录更新了多少条记录
                if (updatedCount > 0) {
                    println("Updated $updatedCount concerts from '待看' to '已看'")
                }
            } catch (e: Exception) {
                // 添加错误处理
                println("Error updating concert statuses: ${e.message}")
            }
        }
    }

    // Performer相关操作
    fun getPerformersByConcertId(concertId: Long): Flow<List<PerformerEntity>> {
        return repository.getPerformersByConcertId(concertId)
    }

    fun insertPerformer(performer: PerformerEntity) {
        viewModelScope.launch {
            repository.insertPerformer(performer)
        }
    }

    fun insertPerformers(performers: List<PerformerEntity>) {
        viewModelScope.launch {
            repository.insertPerformers(performers)
        }
    }

    fun updatePerformer(performer: PerformerEntity) {
        viewModelScope.launch {
            repository.updatePerformer(performer)
        }
    }

    fun deletePerformer(performer: PerformerEntity) {
        viewModelScope.launch {
            repository.deletePerformer(performer)
        }
    }

    fun deletePerformersByConcertId(concertId: Long) {
        viewModelScope.launch {
            repository.deletePerformersByConcertId(concertId)
        }
    }
}