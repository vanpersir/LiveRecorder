package com.ds.liverecorder.domain.repository

import com.ds.liverecorder.domain.model.Concert
import kotlinx.coroutines.flow.Flow

/**
 * 演出仓库接口
 */
interface ConcertRepository {
    /**
     * 获取所有演出
     */
    fun getAllConcerts(): Flow<List<Concert>>

    /**
     * 获取即将开始的演出
     */
    fun getUpcomingConcerts(): Flow<List<Concert>>

    /**
     * 根据ID获取演出
     */
    suspend fun getConcertById(id: Long): Concert?

    /**
     * 插入新演出
     */
    suspend fun insertConcert(concert: Concert)

    /**
     * 更新演出
     */
    suspend fun updateConcert(concert: Concert)

    /**
     * 根据ID删除演出
     */
    suspend fun deleteConcertById(id: Long)

    /**
     * 删除所有演出
     */
    suspend fun deleteAllConcerts()

    /**
     * 搜索演出
     */
    fun searchConcerts(query: String): Flow<List<Concert>>
}