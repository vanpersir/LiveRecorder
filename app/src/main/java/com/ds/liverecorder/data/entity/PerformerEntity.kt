package com.ds.liverecorder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "performers",
    foreignKeys = [ForeignKey(
        entity = ConcertEntity::class,
        parentColumns = ["id"],
        childColumns = ["concertId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["name"], unique = true), Index(value = ["concertId"])])
data class PerformerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    // 添加与Concert的关联关系
    val concertId: Long? = null
)