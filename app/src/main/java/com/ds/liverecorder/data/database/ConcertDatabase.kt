package com.ds.liverecorder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        // 定义从版本1到版本2的迁移 - 添加海报路径字段
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE concerts ADD COLUMN poster_path TEXT")
            }
        }

        // 定义从版本2到版本3的迁移 - 添加票价、实付和其他费用字段
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE concerts ADD COLUMN ticket_price TEXT NOT NULL DEFAULT '0.00'")
                db.execSQL("ALTER TABLE concerts ADD COLUMN ticket_price_currency TEXT NOT NULL DEFAULT 'CNY'")
                db.execSQL("ALTER TABLE concerts ADD COLUMN actual_paid TEXT NOT NULL DEFAULT '0.00'")
                db.execSQL("ALTER TABLE concerts ADD COLUMN actual_paid_currency TEXT NOT NULL DEFAULT 'CNY'")
                db.execSQL("ALTER TABLE concerts ADD COLUMN other_fees TEXT NOT NULL DEFAULT '0.00'")
                db.execSQL("ALTER TABLE concerts ADD COLUMN other_fees_currency TEXT NOT NULL DEFAULT 'CNY'")
            }
        }

        // 定义从版本3到版本4的迁移 - 添加表演者、嘉宾、状态、类别和评分字段
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE concerts ADD COLUMN performers TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE concerts ADD COLUMN guests TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE concerts ADD COLUMN status TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE concerts ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE concerts ADD COLUMN rating INTEGER NOT NULL DEFAULT 0")
            }
        }

        // 定义从版本4到版本5的迁移 - 添加performer表
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `performers` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`concertId` INTEGER)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_performers_name` ON `performers` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_performers_concertId` ON `performers` (`concertId`)")
            }
        }

        // 定义从版本5到版本6的迁移 - 添加外键约束
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建新的performers表，包含外键约束
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `performers_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`concertId` INTEGER, " +
                            "FOREIGN KEY(`concertId`) REFERENCES `concerts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                
                // 复制数据
                db.execSQL("INSERT INTO performers_new (id, name, concertId) SELECT id, name, concertId FROM performers")
                
                // 删除旧表
                db.execSQL("DROP TABLE performers")
                
                // 重命名新表
                db.execSQL("ALTER TABLE performers_new RENAME TO performers")
                
                // 重新创建索引
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_performers_name` ON `performers` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_performers_concertId` ON `performers` (`concertId`)")
            }
        }

        fun getDatabase(context: Context): ConcertDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConcertDatabase::class.java,
                    "concert_database"
                )
                // 添加迁移实现，移除破坏性迁移
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}