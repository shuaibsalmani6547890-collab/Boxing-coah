package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session_records ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionRecordEntity): Long

    @Query("DELETE FROM session_records")
    suspend fun clearAll()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_days ORDER BY dayOfWeek ASC")
    fun getWeeklySchedule(): Flow<List<ScheduleDayEntity>>

    @Query("SELECT * FROM schedule_days ORDER BY dayOfWeek ASC")
    suspend fun getWeeklyScheduleOnce(): List<ScheduleDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: ScheduleDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(days: List<ScheduleDayEntity>)

    @Query("DELETE FROM schedule_days")
    suspend fun clearAll()
}

@Dao
interface ProgressPhotoDao {
    @Query("SELECT * FROM progress_photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<ProgressPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProgressPhotoEntity)

    @Query("DELETE FROM progress_photos WHERE id = :id")
    suspend fun deletePhoto(id: Long)

    @Query("DELETE FROM progress_photos")
    suspend fun clearAll()
}

@Dao
interface CheckInDao {
    @Query("SELECT * FROM daily_checkins ORDER BY dateKey DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: DailyCheckInEntity)

    @Query("DELETE FROM daily_checkins")
    suspend fun clearAll()
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getAllWeights(): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weightLog: WeightLogEntity)

    @Query("DELETE FROM weight_logs")
    suspend fun clearAll()
}
