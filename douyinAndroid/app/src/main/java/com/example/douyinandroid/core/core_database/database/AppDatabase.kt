package com.example.douyinandroid.core.core_database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.douyinandroid.core.core_database.database.dao.UserDao
import com.example.douyinandroid.core.core_database.database.dao.VideoDao
import com.example.douyinandroid.core.core_database.database.entity.*

@Database(
    entities = [
        VideoEntity::class,
        UserEntity::class,
        CacheEntity::class,
        FollowEntity::class,
        LikeEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun userDao(): UserDao

    companion object {
        private const val DATABASE_NAME = "douyin_database"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
