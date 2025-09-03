package com.ds.liverecorder.data.repository

import com.ds.liverecorder.data.dao.ConcertDao
import com.ds.liverecorder.data.dao.PerformerDao
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.entity.PerformerEntity
import kotlinx.coroutines.flow.Flow

class ConcertRepository(
    private val concertDao: ConcertDao,
    private val performerDao: PerformerDao
) {
    fun getAllConcerts(): Flow<List<ConcertEntity>> = concertDao.getAllConcerts()
    
    fun getConcertById(id: Long): Flow<ConcertEntity> = concertDao.getConcertById(id)
    
    suspend fun insertConcert(concert: ConcertEntity): Long {
        return concertDao.insertConcert(concert)
    }
    
    suspend fun updateConcert(concert: ConcertEntity) {
        concertDao.updateConcert(concert)
    }
    
    suspend fun deleteConcert(concert: ConcertEntity) {
        concertDao.deleteConcert(concert)
    }
    
    suspend fun deleteConcertById(id: Long) {
        concertDao.deleteConcertById(id)
    }
    
    // Performer相关操作
    fun getAllPerformers(): Flow<List<PerformerEntity>> = performerDao.getAllPerformers()
    
    fun getPerformersByConcertId(concertId: Long): Flow<List<PerformerEntity>> = 
        performerDao.getPerformersByConcertId(concertId)
    
    suspend fun insertPerformer(performer: PerformerEntity): Long {
        return performerDao.insertPerformer(performer)
    }
    
    suspend fun insertPerformers(performers: List<PerformerEntity>) {
        performerDao.insertPerformers(performers)
    }
    
    suspend fun updatePerformer(performer: PerformerEntity) {
        performerDao.updatePerformer(performer)
    }
    
    suspend fun deletePerformer(performer: PerformerEntity) {
        performerDao.deletePerformer(performer)
    }
    
    suspend fun deletePerformersByConcertId(concertId: Long) {
        performerDao.deletePerformersByConcertId(concertId)
    }
    
    // 新增：获取所有表演者名称（用于自动完成等场景）
    fun getAllPerformersNames(): Flow<List<String>> = concertDao.getAllPerformersNames()
    
    // 新增：根据表演者名称获取其参与的所有演出
    fun getConcertsByPerformerName(performerName: String): Flow<List<ConcertEntity>> = 
        concertDao.getConcertsByPerformerName(performerName)
}