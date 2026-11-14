package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hunter_boxing_prefs")

data class HunterPrefsState(
    val language: AppLanguage = AppLanguage.HINGLISH,
    val themeStyle: AppThemeStyle = AppThemeStyle.CRIMSON_ARENA,
    val voiceEnabled: Boolean = true,
    val useFrontCamera: Boolean = true,
    val weightKg: Float = 60f,
    val goal: TrainingGoal = TrainingGoal.STAMINA,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDate: String = "",
    val mostPunchesOneRound: Int = 0,
    val activePlan: SchedulePlanType = SchedulePlanType.BEGINNER,
    val ownedEquipment: Set<Equipment> = Equipment.entries.toSet(),
    val unlockedBadges: Set<String> = emptySet(),
    val questDate: String = "",
    val questJabsToday: Int = 0,
    val questRoundsToday: Int = 0,
    val questSkippingMinToday: Int = 0,
    val claimedQuestIds: Set<String> = emptySet()
)

class UserPreferencesManager(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_STYLE = stringPreferencesKey("theme_style")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val FRONT_CAMERA = booleanPreferencesKey("front_camera")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val GOAL = stringPreferencesKey("goal")
        val XP = intPreferencesKey("xp")
        val LEVEL = intPreferencesKey("level")
        val STREAK = intPreferencesKey("streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_WORKOUT_DATE = stringPreferencesKey("last_workout_date")
        val MOST_PUNCHES_ROUND = intPreferencesKey("most_punches_round")
        val ACTIVE_PLAN = stringPreferencesKey("active_plan")
        val OWNED_EQUIPMENT = stringSetPreferencesKey("owned_equipment")
        val UNLOCKED_BADGES = stringSetPreferencesKey("unlocked_badges")
        val QUEST_DATE = stringPreferencesKey("quest_date")
        val QUEST_JABS = intPreferencesKey("quest_jabs")
        val QUEST_ROUNDS = intPreferencesKey("quest_rounds")
        val QUEST_SKIPPING = intPreferencesKey("quest_skipping")
        val CLAIMED_QUESTS = stringSetPreferencesKey("claimed_quests")
    }

    val prefsFlow: Flow<HunterPrefsState> = context.dataStore.data.map { p ->
        val today = todayKey()
        val savedQuestDate = p[Keys.QUEST_DATE] ?: today
        val sameDay = savedQuestDate == today

        val lang = runCatching { AppLanguage.valueOf(p[Keys.LANGUAGE] ?: AppLanguage.HINGLISH.name) }
            .getOrDefault(AppLanguage.HINGLISH)
        val theme = runCatching { AppThemeStyle.valueOf(p[Keys.THEME_STYLE] ?: AppThemeStyle.CRIMSON_ARENA.name) }
            .getOrDefault(AppThemeStyle.CRIMSON_ARENA)
        val goal = runCatching { TrainingGoal.valueOf(p[Keys.GOAL] ?: TrainingGoal.STAMINA.name) }
            .getOrDefault(TrainingGoal.STAMINA)
        val plan = runCatching { SchedulePlanType.valueOf(p[Keys.ACTIVE_PLAN] ?: SchedulePlanType.BEGINNER.name) }
            .getOrDefault(SchedulePlanType.BEGINNER)
        val defaultEquip = Equipment.entries.map { it.name }.toSet()
        val equipSet = (p[Keys.OWNED_EQUIPMENT] ?: defaultEquip)
            .mapNotNull { runCatching { Equipment.valueOf(it) }.getOrNull() }
            .toSet() + Equipment.NONE

        HunterPrefsState(
            language = lang,
            themeStyle = theme,
            voiceEnabled = p[Keys.VOICE_ENABLED] ?: true,
            useFrontCamera = p[Keys.FRONT_CAMERA] ?: true,
            weightKg = p[Keys.WEIGHT_KG] ?: 60f,
            goal = goal,
            xp = p[Keys.XP] ?: 0,
            level = p[Keys.LEVEL] ?: 1,
            streak = p[Keys.STREAK] ?: 0,
            longestStreak = p[Keys.LONGEST_STREAK] ?: 0,
            lastWorkoutDate = p[Keys.LAST_WORKOUT_DATE] ?: "",
            mostPunchesOneRound = p[Keys.MOST_PUNCHES_ROUND] ?: 0,
            activePlan = plan,
            ownedEquipment = equipSet,
            unlockedBadges = p[Keys.UNLOCKED_BADGES] ?: emptySet(),
            questDate = savedQuestDate,
            questJabsToday = if (sameDay) p[Keys.QUEST_JABS] ?: 0 else 0,
            questRoundsToday = if (sameDay) p[Keys.QUEST_ROUNDS] ?: 0 else 0,
            questSkippingMinToday = if (sameDay) p[Keys.QUEST_SKIPPING] ?: 0 else 0,
            claimedQuestIds = if (sameDay) p[Keys.CLAIMED_QUESTS] ?: emptySet() else emptySet()
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setThemeStyle(themeStyle: AppThemeStyle) {
        context.dataStore.edit { it[Keys.THEME_STYLE] = themeStyle.name }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_ENABLED] = enabled }
    }

    suspend fun setUseFrontCamera(front: Boolean) {
        context.dataStore.edit { it[Keys.FRONT_CAMERA] = front }
    }

    suspend fun setWeightKg(weight: Float) {
        context.dataStore.edit { it[Keys.WEIGHT_KG] = weight.coerceIn(30f, 180f) }
    }

    suspend fun setGoal(goal: TrainingGoal) {
        context.dataStore.edit { it[Keys.GOAL] = goal.name }
    }

    suspend fun toggleEquipment(eq: Equipment) {
        if (eq == Equipment.NONE) return
        context.dataStore.edit { p ->
            val current = (p[Keys.OWNED_EQUIPMENT] ?: setOf(Equipment.NONE.name)).toMutableSet()
            if (current.contains(eq.name)) current.remove(eq.name) else current.add(eq.name)
            current.add(Equipment.NONE.name)
            p[Keys.OWNED_EQUIPMENT] = current
        }
    }

    suspend fun setActivePlan(plan: SchedulePlanType) {
        context.dataStore.edit { it[Keys.ACTIVE_PLAN] = plan.name }
    }

    suspend fun addXpAndUpdateProgress(
        earnedXp: Int,
        punchesInSession: Int,
        jabsCount: Int,
        roundsCompleted: Int,
        guardPct: Int
    ): Int? {
        var leveledUpTo: Int? = null
        val today = todayKey()
        val yesterday = yesterdayKey()

        context.dataStore.edit { p ->
            val currentXp = (p[Keys.XP] ?: 0) + earnedXp
            var level = p[Keys.LEVEL] ?: 1
            var remainingXp = currentXp
            while (remainingXp >= xpForNextLevel(level)) {
                remainingXp -= xpForNextLevel(level)
                level += 1
                leveledUpTo = level
            }
            p[Keys.XP] = remainingXp
            p[Keys.LEVEL] = level

            val lastDate = p[Keys.LAST_WORKOUT_DATE] ?: ""
            val oldStreak = p[Keys.STREAK] ?: 0
            val newStreak = when (lastDate) {
                today -> oldStreak.coerceAtLeast(1)
                yesterday -> oldStreak + 1
                else -> 1
            }
            p[Keys.STREAK] = newStreak
            p[Keys.LAST_WORKOUT_DATE] = today
            val longest = maxOf(p[Keys.LONGEST_STREAK] ?: 0, newStreak)
            p[Keys.LONGEST_STREAK] = longest

            val prevBest = p[Keys.MOST_PUNCHES_ROUND] ?: 0
            if (punchesInSession > prevBest) {
                p[Keys.MOST_PUNCHES_ROUND] = punchesInSession
            }

            val savedDate = p[Keys.QUEST_DATE] ?: ""
            val sameDay = savedDate == today
            p[Keys.QUEST_DATE] = today
            p[Keys.QUEST_JABS] = (if (sameDay) p[Keys.QUEST_JABS] ?: 0 else 0) + jabsCount
            p[Keys.QUEST_ROUNDS] = (if (sameDay) p[Keys.QUEST_ROUNDS] ?: 0 else 0) + roundsCompleted
            if (!sameDay) {
                p[Keys.QUEST_SKIPPING] = 0
                p[Keys.CLAIMED_QUESTS] = emptySet()
            }

            val badges = (p[Keys.UNLOCKED_BADGES] ?: emptySet()).toMutableSet()
            badges.add("first_blood")
            if (punchesInSession >= 50) badges.add("jab_master")
            if (guardPct >= 85) badges.add("iron_guard")
            if (newStreak >= 7) badges.add("streak_7")
            if (level >= 22) badges.add("s_rank")
            p[Keys.UNLOCKED_BADGES] = badges
        }
        return leveledUpTo
    }

    suspend fun logSkippingMinutes(minutes: Int) {
        val today = todayKey()
        context.dataStore.edit { p ->
            val sameDay = (p[Keys.QUEST_DATE] ?: "") == today
            p[Keys.QUEST_DATE] = today
            p[Keys.QUEST_SKIPPING] = (if (sameDay) p[Keys.QUEST_SKIPPING] ?: 0 else 0) + minutes
            if (!sameDay) {
                p[Keys.QUEST_JABS] = 0
                p[Keys.QUEST_ROUNDS] = 0
                p[Keys.CLAIMED_QUESTS] = emptySet()
            }
        }
    }

    suspend fun claimQuest(questId: String, rewardXp: Int): Int? {
        var leveledUpTo: Int? = null
        val today = todayKey()
        context.dataStore.edit { p ->
            val sameDay = (p[Keys.QUEST_DATE] ?: "") == today
            val claimed = (if (sameDay) p[Keys.CLAIMED_QUESTS] ?: emptySet() else emptySet()).toMutableSet()
            if (claimed.contains(questId)) return@edit
            claimed.add(questId)
            p[Keys.QUEST_DATE] = today
            p[Keys.CLAIMED_QUESTS] = claimed

            var remainingXp = (p[Keys.XP] ?: 0) + rewardXp
            var level = p[Keys.LEVEL] ?: 1
            while (remainingXp >= xpForNextLevel(level)) {
                remainingXp -= xpForNextLevel(level)
                level += 1
                leveledUpTo = level
            }
            p[Keys.XP] = remainingXp
            p[Keys.LEVEL] = level
            if (level >= 22) {
                val badges = (p[Keys.UNLOCKED_BADGES] ?: emptySet()).toMutableSet()
                badges.add("s_rank")
                p[Keys.UNLOCKED_BADGES] = badges
            }
        }
        return leveledUpTo
    }

    suspend fun restoreFromBackup(backup: FullBackupPayload) {
        context.dataStore.edit { p ->
            p[Keys.WEIGHT_KG] = backup.weightKg
            p[Keys.GOAL] = backup.goal
            p[Keys.XP] = backup.xp
            p[Keys.LEVEL] = backup.level
            p[Keys.STREAK] = backup.streak
            p[Keys.LONGEST_STREAK] = backup.longestStreak
            p[Keys.MOST_PUNCHES_ROUND] = backup.mostPunchesOneRound
            p[Keys.LANGUAGE] = backup.language
            p[Keys.ACTIVE_PLAN] = backup.activePlan
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        fun xpForNextLevel(level: Int): Int = 150 + (level - 1) * 75

        fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        fun yesterdayKey(): String {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        }
    }
}
