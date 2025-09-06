package com.ds.liverecorder.data.repository

import com.ds.liverecorder.data.dao.ConcertDao
import com.ds.liverecorder.domain.mapper.ConcertMapper
import com.ds.liverecorder.domain.model.Concert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import com.ds.liverecorder.domain.repository.ConcertRepository as DomainConcertRepository

/**
 * 演出仓库实现类
 */
class ConcertRepositoryImpl(private val concertDao: ConcertDao) : DomainConcertRepository {

    private val mapper by lazy { ConcertMapper() }
    
    override fun getAllConcerts(): Flow<List<Concert>> {
        return concertDao.getAllConcerts().map { entities ->
            entities.map { mapper.toDomainModel(it) }
        }
    }

    override fun getUpcomingConcerts(): Flow<List<Concert>> {
        return concertDao.getConcertsAfterDate(Date()).map { entities ->
            entities.map { mapper.toDomainModel(it) }
        }
    }

    override suspend fun getConcertById(id: Long): Concert? {
        return concertDao.getConcertById(id)?.let { mapper.toDomainModel(it) }
    }

    override suspend fun insertConcert(concert: Concert) {
        concertDao.insertConcert(mapper.toEntity(concert))
    }

    override suspend fun updateConcert(concert: Concert) {
        concertDao.updateConcert(mapper.toEntity(concert))
    }

    override suspend fun deleteConcertById(id: Long) {
        concertDao.deleteConcertById(id)
    }

    override suspend fun deleteAllConcerts() {
        concertDao.deleteAllConcerts()
    }

    override fun searchConcerts(query: String): Flow<List<Concert>> {
        return concertDao.getAllConcerts().map { entities ->
            entities.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.venue.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.performers.any { performer -> performer.contains(query, ignoreCase = true) } ||
                it.guests.any { guest -> guest.contains(query, ignoreCase = true) }
            }.map { mapper.toDomainModel(it) }
        }
    }
}