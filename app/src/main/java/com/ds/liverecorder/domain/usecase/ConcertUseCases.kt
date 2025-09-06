package com.ds.liverecorder.domain.usecase

import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.repository.ConcertRepository
import kotlinx.coroutines.flow.Flow

/**
 * 演出相关业务逻辑的统一入口
 */
class ConcertUseCases(
    private val repository: ConcertRepository
) {
    /**
     * 获取所有演出
     */
    fun getAllConcerts(): Flow<List<Concert>> {
        return repository.getAllConcerts()
    }

    /**
     * 获取即将到来的演出
     */
    fun getUpcomingConcerts(): Flow<List<Concert>> {
        return repository.getUpcomingConcerts()
    }

    /**
     * 根据ID获取演出
     */
    suspend fun getConcertById(id: Long): Result<Concert?> {
        return try {
            val concert = repository.getConcertById(id)
            Result.success(concert)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 添加新演出
     */
    suspend fun addConcert(concert: Concert): Result<Unit> {
        return try {
            // 验证演出数据
            validateConcert(concert)
            repository.insertConcert(concert)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新演出
     */
    suspend fun updateConcert(concert: Concert): Result<Unit> {
        return try {
            // 验证演出数据
            validateConcert(concert)
            repository.updateConcert(concert)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除演出
     */
    suspend fun deleteConcert(id: Long): Result<Unit> {
        return try {
            repository.deleteConcertById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 搜索演出
     */
    fun searchConcerts(query: String): Flow<List<Concert>> {
        return repository.searchConcerts(query)
    }

    /**
     * 验证演出数据
     */
    private fun validateConcert(concert: Concert) {
        if (concert.title.isBlank()) {
            throw IllegalArgumentException("演出名称不能为空")
        }
        
        if (concert.venue.isBlank()) {
            throw IllegalArgumentException("演出场地不能为空")
        }
        
        if (concert.date.time <= 0) {
            throw IllegalArgumentException("演出日期无效")
        }
    }
}