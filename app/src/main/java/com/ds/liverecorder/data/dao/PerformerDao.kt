package com.ds.liverecorder.data.dao

import androidx.room.*
import com.ds.liverecorder.data.entity.PerformerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformerDao {
    @Query("SELECT * FROM performers")
    fun getAllPerformers(): Flow<List<PerformerEntity>>

    @Query("SELECT * FROM performers WHERE id = :id")
    fun getPerformerById(id: Long): Flow<PerformerEntity>

    @Query("SELECT * FROM performers WHERE concertId = :concertId")
    fun getPerformersByConcertId(concertId: Long): Flow<List<PerformerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformer(performer: PerformerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformers(performers: List<PerformerEntity>)

    @Update
    suspend fun updatePerformer(performer: PerformerEntity)

    @Delete
    suspend fun deletePerformer(performer: PerformerEntity)

    @Query("DELETE FROM performers WHERE concertId = :concertId")
    suspend fun deletePerformersByConcertId(concertId: Long)
}