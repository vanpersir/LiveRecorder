package com.ds.liverecorder.data.dao

import androidx.room.*
import com.ds.liverecorder.data.entity.ConcertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConcertDao {
    @Query("SELECT * FROM concerts")
    fun getAllConcerts(): Flow<List<ConcertEntity>>

    @Query("SELECT * FROM concerts WHERE id = :id")
    fun getConcertById(id: Long): Flow<ConcertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcert(concert: ConcertEntity): Long

    @Update
    suspend fun updateConcert(concert: ConcertEntity)

    @Delete
    suspend fun deleteConcert(concert: ConcertEntity)

    @Query("DELETE FROM concerts WHERE id = :id")
    suspend fun deleteConcertById(id: Long)

    // 新增：获取所有表演者
    @Query("SELECT DISTINCT name FROM performers")
    fun getAllPerformersNames(): Flow<List<String>>

    // 新增：根据表演者名称获取其参与的所有演出
    @Query("SELECT c.* FROM concerts c JOIN performers p ON c.id = p.concertId WHERE p.name = :performerName")
    fun getConcertsByPerformerName(performerName: String): Flow<List<ConcertEntity>>
}