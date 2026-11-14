package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_records")
data class SessionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int,
    val totalPunches: Int,
    val avgScore: Int,
    val guardDisciplinePct: Int,
    val footworkScorePct: Int,
    val bestPunchType: String,
    val bestPunchScore: Int,
    val worstPunchType: String,
    val worstPunchScore: Int,
    val commonMistakesJson: String,
    val comboName: String,
    val videoPath: String? = null,
    val poseTimelineJson: String? = null
)

@Entity(tableName = "schedule_days")
data class ScheduleDayEntity(
    @PrimaryKey val dayOfWeek: Int, // 1 = Mon .. 7 = Sun
    val dayTitle: String,
    val isRestDay: Boolean,
    val isRecoveryDay: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val reminderEnabled: Boolean,
    val exercisesJson: String
)

@Entity(tableName = "progress_photos")
data class ProgressPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uriString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Float,
    val note: String
)

@Entity(tableName = "daily_checkins")
data class DailyCheckInEntity(
    @PrimaryKey val dateKey: String, // YYYY-MM-DD
    val mood: Int,
    val sleepHours: Float,
    val soreness: Int,
    val intensityRecommendation: String,
    val disciplineFollowed: Boolean
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Float
)
