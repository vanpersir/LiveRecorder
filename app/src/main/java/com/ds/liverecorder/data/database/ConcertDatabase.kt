package com.ds.liverecorder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ds.liverecorder.data.converter.Converters
import com.ds.liverecorder.data.dao.ConcertDao
import com.ds.liverecorder.data.dao.PerformerDao
import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.data.entity.PerformerEntity

@Database(
    entities = [ConcertEntity::class, PerformerEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ConcertDatabase : RoomDatabase() {

    abstract fun concertDao(): ConcertDao
    abstract fun performerDao(): PerformerDao

    companion object {
        @Volatile
        private var INSTANCE: ConcertDatabase? = null

        fun getDatabase(context: Context): ConcertDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConcertDatabase::class.java,
                    "concert_database"
                )
                .fallbackToDestructiveMigration() // 添加破坏性迁移
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}