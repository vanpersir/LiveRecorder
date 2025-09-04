package com.ds.liverecorder.data.dao

import androidx.room.*
import com.ds.liverecorder.data.entity.ConcertEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ConcertDao {
    @Query("SELECT * FROM concerts ORDER BY date ASC")
    fun getAllConcerts(): Flow<List<ConcertEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConcert(concert: ConcertEntity)

    @Update
    suspend fun updateConcert(concert: ConcertEntity)

    @Delete
    suspend fun deleteConcert(concert: ConcertEntity)

    @Query("DELETE FROM concerts WHERE id = :id")
    suspend fun deleteConcertById(id: Long)

    @Query("DELETE FROM concerts")
    suspend fun deleteAllConcerts()
    
    @Query("SELECT * FROM concerts WHERE id = :id")
    suspend fun getConcertById(id: Long): ConcertEntity?
    
    @Query("SELECT DISTINCT performers FROM concerts")
    fun getAllPerformersNames(): Flow<List<String>>
    
    @Query("SELECT * FROM concerts WHERE performers LIKE '%' || :performerName || '%' ORDER BY date ASC")
    fun getConcertsByPerformerName(performerName: String): Flow<List<ConcertEntity>>
    
    @Query("SELECT * FROM concerts WHERE date >= :date ORDER BY date ASC")
    fun getConcertsAfterDate(date: Date): Flow<List<ConcertEntity>>
}