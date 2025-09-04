package com.ds.liverecorder.data.repository

import androidx.annotation.WorkerThread
import com.ds.liverecorder.data.dao.ConcertDao
import com.ds.liverecorder.data.entity.ConcertEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class ConcertRepository(private val concertDao: ConcertDao) {
    val allConcerts: Flow<List<ConcertEntity>> = concertDao.getAllConcerts()
    val allPerformersNames: Flow<List<String>> = concertDao.getAllPerformersNames()

    @WorkerThread
    suspend fun insertConcert(concert: ConcertEntity) {
        concertDao.insertConcert(concert)
    }

    @WorkerThread
    suspend fun updateConcert(concert: ConcertEntity) {
        concertDao.updateConcert(concert)
    }

    @WorkerThread
    suspend fun deleteConcert(concert: ConcertEntity) {
        concertDao.deleteConcert(concert)
    }

    @WorkerThread
    suspend fun deleteConcertById(id: Long) {
        concertDao.deleteConcertById(id)
    }

    @WorkerThread
    suspend fun deleteAllConcerts() {
        concertDao.deleteAllConcerts()
    }
    
    @WorkerThread
    suspend fun getConcertById(id: Long): ConcertEntity? {
        return concertDao.getConcertById(id)
    }
    
    fun getConcertByIdAsFlow(id: Long): Flow<ConcertEntity?> = flow {
        emit(concertDao.getConcertById(id))
    }
    
    fun getConcertsByPerformerName(performerName: String): Flow<List<ConcertEntity>> {
        return concertDao.getConcertsByPerformerName(performerName)
    }
    
    fun getConcertsAfterDate(date: Date): Flow<List<ConcertEntity>> {
        return concertDao.getConcertsAfterDate(date)
    }
}