package com.example.data.local

import com.example.data.catalog.BoxingCatalog
import com.example.data.model.FullBackupPayload
import com.example.data.model.SchedulePlanType
import com.example.data.model.ScheduledExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BoxingRepository(
    private val db: AppDatabase,
    val prefs: UserPreferencesManager
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    val sessions: Flow<List<SessionRecordEntity>> = db.sessionDao().getAllSessions()
    val weeklySchedule: Flow<List<ScheduleDayEntity>> = db.scheduleDao().getWeeklySchedule()
    val progressPhotos: Flow<List<ProgressPhotoEntity>> = db.progressPhotoDao().getAllPhotos()
    val checkIns: Flow<List<DailyCheckInEntity>> = db.checkInDao().getAllCheckIns()
    val weightLogs: Flow<List<WeightLogEntity>> = db.weightLogDao().getAllWeights()

    suspend fun ensureInitialScheduleSeeded() {
        if (db.scheduleDao().getWeeklyScheduleOnce().isEmpty()) {
            applySchedulePlan(SchedulePlanType.BEGINNER)
        }
    }

    suspend fun applySchedulePlan(plan: SchedulePlanType) {
        prefs.setActivePlan(plan)
        if (plan == SchedulePlanType.CUSTOM && db.scheduleDao().getWeeklyScheduleOnce().isNotEmpty()) {
            return
        }
        val rows = BoxingCatalog.defaultScheduleFor(plan).map { (day, title, pair) ->
            ScheduleDayEntity(
                dayOfWeek = day,
                dayTitle = title,
                isRestDay = pair.first,
                isRecoveryDay = title.contains("Recovery", ignoreCase = true),
                reminderHour = 7,
                reminderMinute = 0,
                reminderEnabled = !pair.first,
                exercisesJson = json.encodeToString(pair.second)
            )
        }
        db.scheduleDao().upsertAll(rows)
    }

    suspend fun upsertScheduleDay(day: ScheduleDayEntity) {
        db.scheduleDao().upsertDay(day)
        prefs.setActivePlan(SchedulePlanType.CUSTOM)
    }

    fun decodeExercises(raw: String): List<ScheduledExercise> {
        return runCatching { json.decodeFromString<List<ScheduledExercise>>(raw) }
            .getOrDefault(emptyList())
    }

    fun encodeExercises(list: List<ScheduledExercise>): String = json.encodeToString(list)

    suspend fun saveSession(session: SessionRecordEntity): Long = db.sessionDao().insertSession(session)

    suspend fun saveCheckIn(checkIn: DailyCheckInEntity) = db.checkInDao().upsertCheckIn(checkIn)

    suspend fun addWeightLog(weightKg: Float) {
        prefs.setWeightKg(weightKg)
        db.weightLogDao().insertWeight(WeightLogEntity(weightKg = weightKg))
    }

    suspend fun addProgressPhoto(uriString: String, weightKg: Float, note: String) {
        db.progressPhotoDao().insertPhoto(
            ProgressPhotoEntity(uriString = uriString, weightKg = weightKg, note = note)
        )
    }

    suspend fun deleteProgressPhoto(id: Long) {
        db.progressPhotoDao().deletePhoto(id)
    }

    suspend fun exportBackupJson(): String {
        val state = prefs.prefsFlow.first()
        val payload = FullBackupPayload(
            weightKg = state.weightKg,
            goal = state.goal.name,
            xp = state.xp,
            level = state.level,
            streak = state.streak,
            longestStreak = state.longestStreak,
            mostPunchesOneRound = state.mostPunchesOneRound,
            language = state.language.name,
            activePlan = state.activePlan.name
        )
        return json.encodeToString(payload)
    }

    suspend fun importBackupJson(rawJson: String): Boolean {
        return runCatching {
            val payload = json.decodeFromString<FullBackupPayload>(rawJson)
            prefs.restoreFromBackup(payload)
            true
        }.getOrDefault(false)
    }

    suspend fun resetAllData() {
        db.sessionDao().clearAll()
        db.scheduleDao().clearAll()
        db.progressPhotoDao().clearAll()
        db.checkInDao().clearAll()
        db.weightLogDao().clearAll()
        prefs.resetAll()
        applySchedulePlan(SchedulePlanType.BEGINNER)
    }
}
