package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SessionRecordEntity::class,
        ScheduleDayEntity::class,
        ProgressPhotoEntity::class,
        DailyCheckInEntity::class,
        WeightLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun checkInDao(): CheckInDao
    abstract fun weightLogDao(): WeightLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shadow_boxing_offline.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
