package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BellSoundPlayer
import com.example.audio.VoiceCoach
import com.example.data.catalog.BoxingCatalog
import com.example.data.local.*
import com.example.data.model.*
import com.example.i18n.AppStrings
import com.example.notifications.ReminderScheduler
import com.example.pose.BoxingBiomechanicsEngine
import com.example.pose.LivePoseState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.UUID

data class SessionSummaryUi(
    val totalPunches: Int,
    val avgScore: Int,
    val guardPct: Int,
    val footworkPct: Int,
    val bestPunch: String,
    val worstPunch: String,
    val topMistakes: List<String>,
    val xpEarned: Int,
    val leveledUpTo: Int?,
    val videoPath: String?,
    val poseTimelineJson: String?
)

data class LiveCoachUiState(
    val sessionActive: Boolean = false,
    val isRecording: Boolean = false,
    val comboMode: Boolean = false,
    val selectedCombo: BoxingCombo = BoxingCatalog.presetCombos.first(),
    val comboStepIndex: Int = 0,
    val combosCompleted: Int = 0,
    val roundDurationSec: Int = 180,
    val restDurationSec: Int = 60,
    val totalRounds: Int = 3,
    val currentRound: Int = 1,
    val isRestPhase: Boolean = false,
    val timerSecondsLeft: Int = 180,
    val timerRunning: Boolean = false,
    val livePose: LivePoseState = LivePoseState(),
    val lastPunch: PunchFeedbackEvent? = null,
    val liveBannerHi: String = "Stance me aao aur pehla punch maaro!",
    val liveBannerEn: String = "Get in stance and throw your first punch!",
    val sessionPunches: List<PunchFeedbackEvent> = emptyList(),
    val summary: SessionSummaryUi? = null
)

data class WeeklyReportUi(
    val sessionsThisWeek: Int,
    val avgFormScore: Int,
    val improvedTextHi: String,
    val improvedTextEn: String,
    val weakPointHi: String,
    val weakPointEn: String,
    val nextFocusHi: String,
    val nextFocusEn: String
)

class BoxingCoachViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val prefsManager = UserPreferencesManager(application)
    val repo = BoxingRepository(db, prefsManager)
    val voiceCoach = VoiceCoach(application)
    private val json = Json { ignoreUnknownKeys = true }

    val prefsState: StateFlow<HunterPrefsState> = prefsManager.prefsFlow
        .onEach { voiceCoach.setLanguage(it.language) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HunterPrefsState())

    val sessions: StateFlow<List<SessionRecordEntity>> = repo.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklySchedule: StateFlow<List<ScheduleDayEntity>> = repo.weeklySchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressPhotos: StateFlow<List<ProgressPhotoEntity>> = repo.progressPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkIns: StateFlow<List<DailyCheckInEntity>> = repo.checkIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs: StateFlow<List<WeightLogEntity>> = repo.weightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _liveCoachState = MutableStateFlow(LiveCoachUiState())
    val liveCoachState: StateFlow<LiveCoachUiState> = _liveCoachState.asStateFlow()

    private val _customCombo = MutableStateFlow(listOf("JAB", "CROSS", "LEAD_HOOK"))
    val customCombo: StateFlow<List<String>> = _customCombo.asStateFlow()

    private val _quoteIndex = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
    val quoteIndex: StateFlow<Int> = _quoteIndex.asStateFlow()

    private val _statusToast = MutableStateFlow<String?>(null)
    val statusToast: StateFlow<String?> = _statusToast.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartMs: Long = 0L
    private var lastRecordedVideoPath: String? = null
    private val poseTimelineBuffer = mutableListOf<PoseFrameSnapshot>()
    private var guardGoodFrames = 0
    private var totalPoseFrames = 0
    private var footworkScoreSum = 0

    init {
        viewModelScope.launch {
            repo.ensureInitialScheduleSeeded()
        }
    }

    fun clearToast() {
        _statusToast.value = null
    }

    fun nextQuote() {
        _quoteIndex.value = _quoteIndex.value + 1
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { prefsManager.setLanguage(lang) }
    }

    fun setThemeStyle(style: AppThemeStyle) {
        viewModelScope.launch { prefsManager.setThemeStyle(style) }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setVoiceEnabled(enabled) }
    }

    fun toggleCameraFacing() {
        viewModelScope.launch {
            prefsManager.setUseFrontCamera(!prefsState.value.useFrontCamera)
        }
    }

    fun setUseFrontCamera(front: Boolean) {
        viewModelScope.launch { prefsManager.setUseFrontCamera(front) }
    }

    fun updateWeight(weightKg: Float) {
        viewModelScope.launch { repo.addWeightLog(weightKg) }
    }

    fun setGoal(goal: TrainingGoal) {
        viewModelScope.launch { prefsManager.setGoal(goal) }
    }

    fun toggleEquipment(equipment: Equipment) {
        viewModelScope.launch { prefsManager.toggleEquipment(equipment) }
    }

    fun selectSchedulePlan(plan: SchedulePlanType) {
        viewModelScope.launch { repo.applySchedulePlan(plan) }
    }

    fun saveDailyCheckIn(mood: Int, sleepHours: Float, soreness: Int) {
        viewModelScope.launch {
            val intensity = when {
                soreness >= 4 || sleepHours < 5.5f -> "LIGHT"
                mood >= 4 && soreness <= 2 && sleepHours >= 7f -> "HEAVY"
                else -> "MODERATE"
            }
            repo.saveCheckIn(
                DailyCheckInEntity(
                    dateKey = UserPreferencesManager.todayKey(),
                    mood = mood,
                    sleepHours = sleepHours,
                    soreness = soreness,
                    intensityRecommendation = intensity,
                    disciplineFollowed = true
                )
            )
            _statusToast.value = if (prefsState.value.language == AppLanguage.HINGLISH)
                "Check-In saved! Aaj ka level: $intensity" else "Check-In saved! Intensity: $intensity"
        }
    }

    fun dailyQuests(): List<DailyQuest> {
        val p = prefsState.value
        return listOf(
            DailyQuest(
                id = "q_jabs",
                titleHi = "50 Jab Maaro",
                titleEn = "Throw 50 Jabs",
                target = 50,
                current = p.questJabsToday.coerceAtMost(50),
                xpReward = 80,
                claimed = p.claimedQuestIds.contains("q_jabs")
            ),
            DailyQuest(
                id = "q_rounds",
                titleHi = "3 Round Shadow Boxing",
                titleEn = "3 Rounds Shadow Boxing",
                target = 3,
                current = p.questRoundsToday.coerceAtMost(3),
                xpReward = 100,
                claimed = p.claimedQuestIds.contains("q_rounds")
            ),
            DailyQuest(
                id = "q_skip",
                titleHi = "10 Min Skipping",
                titleEn = "10 Min Skipping",
                target = 10,
                current = p.questSkippingMinToday.coerceAtMost(10),
                xpReward = 70,
                claimed = p.claimedQuestIds.contains("q_skip")
            )
        )
    }

    fun logQuickSkipping(minutes: Int = 5) {
        viewModelScope.launch {
            prefsManager.logSkippingMinutes(minutes)
            _statusToast.value = if (prefsState.value.language == AppLanguage.HINGLISH)
                "+$minutes min Skipping logged!" else "+$minutes min Skipping logged!"
        }
    }

    fun claimQuest(quest: DailyQuest) {
        if (quest.claimed || quest.current < quest.target) return
        viewModelScope.launch {
            val lvlUp = prefsManager.claimQuest(quest.id, quest.xpReward)
            _statusToast.value = if (lvlUp != null) {
                "LEVEL UP! Hunter Level $lvlUp!"
            } else {
                "+${quest.xpReward} XP Claimed!"
            }
        }
    }

    // --- Live Coach Controls ---
    fun configureTimer(roundSec: Int, restSec: Int, rounds: Int) {
        _liveCoachState.update {
            it.copy(
                roundDurationSec = roundSec,
                restDurationSec = restSec,
                totalRounds = rounds,
                timerSecondsLeft = if (!it.sessionActive) roundSec else it.timerSecondsLeft
            )
        }
    }

    fun setComboMode(enabled: Boolean, combo: BoxingCombo? = null) {
        _liveCoachState.update {
            it.copy(
                comboMode = enabled,
                selectedCombo = combo ?: it.selectedCombo,
                comboStepIndex = 0
            )
        }
        if (enabled && prefsState.value.voiceEnabled) {
            val activeCombo = combo ?: _liveCoachState.value.selectedCombo
            val call = if (prefsState.value.language == AppLanguage.HINGLISH) activeCombo.voiceCallHi else activeCombo.voiceCallEn
            voiceCoach.speakFeedback(call, force = true)
        }
    }

    fun launchComboInCoach(combo: BoxingCombo) {
        setComboMode(true, combo)
    }

    fun addStepToCustomCombo(move: String) {
        if (_customCombo.value.size < 6) {
            _customCombo.value = _customCombo.value + move
        }
    }

    fun clearCustomCombo() {
        _customCombo.value = emptyList()
    }

    fun buildCustomComboObject(): BoxingCombo {
        val seq = _customCombo.value.ifEmpty { listOf("JAB", "CROSS") }
        val code = seq.joinToString("-") {
            when (it) {
                "JAB" -> "1"
                "CROSS" -> "2"
                "LEAD_HOOK" -> "3"
                "UPPERCUT" -> "5"
                else -> "S"
            }
        }
        val names = seq.joinToString(", ") {
            when (it) {
                "JAB" -> "Jab"
                "CROSS" -> "Cross"
                "LEAD_HOOK" -> "Hook"
                "UPPERCUT" -> "Uppercut"
                else -> "Slip"
            }
        }
        return BoxingCombo(
            id = "custom_$code",
            code = code,
            nameHi = "Custom ($code)",
            nameEn = "Custom ($code)",
            sequence = seq,
            voiceCallHi = names,
            voiceCallEn = names,
            difficulty = "Custom"
        )
    }

    fun startLiveSession() {
        sessionStartMs = System.currentTimeMillis()
        lastRecordedVideoPath = null
        poseTimelineBuffer.clear()
        guardGoodFrames = 0
        totalPoseFrames = 0
        footworkScoreSum = 0

        _liveCoachState.update {
            it.copy(
                sessionActive = true,
                currentRound = 1,
                isRestPhase = false,
                timerSecondsLeft = it.roundDurationSec,
                timerRunning = true,
                sessionPunches = emptyList(),
                lastPunch = null,
                comboStepIndex = 0,
                combosCompleted = 0,
                summary = null
            )
        }
        BellSoundPlayer.playBoxingBell()
        if (_liveCoachState.value.comboMode && prefsState.value.voiceEnabled) {
            val c = _liveCoachState.value.selectedCombo
            val call = if (prefsState.value.language == AppLanguage.HINGLISH) c.voiceCallHi else c.voiceCallEn
            voiceCoach.speakFeedback(call, force = true)
        }
        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_liveCoachState.value.sessionActive) {
                delay(1000L)
                val st = _liveCoachState.value
                if (!st.timerRunning) continue
                val next = st.timerSecondsLeft - 1
                if (next == 10 && !st.isRestPhase) {
                    BellSoundPlayer.playClapperWarning()
                }
                if (next <= 0) {
                    BellSoundPlayer.playBoxingBell()
                    if (!st.isRestPhase) {
                        if (st.currentRound >= st.totalRounds) {
                            endLiveSession()
                            break
                        } else {
                            _liveCoachState.update {
                                it.copy(isRestPhase = true, timerSecondsLeft = it.restDurationSec)
                            }
                            if (prefsState.value.voiceEnabled) {
                                val msg = if (prefsState.value.language == AppLanguage.HINGLISH)
                                    "Round khatam! Rest karo aur gehri saans lo." else "Round over! Breathe and recover."
                                voiceCoach.speakFeedback(msg, force = true)
                            }
                        }
                    } else {
                        val nextRound = st.currentRound + 1
                        _liveCoachState.update {
                            it.copy(
                                isRestPhase = false,
                                currentRound = nextRound,
                                timerSecondsLeft = it.roundDurationSec
                            )
                        }
                        if (prefsState.value.voiceEnabled) {
                            voiceCoach.speakFeedback("Round $nextRound! Fight!", force = true)
                        }
                    }
                } else {
                    _liveCoachState.update { it.copy(timerSecondsLeft = next) }
                }
            }
        }
    }

    fun toggleRecordingState(recording: Boolean, savedPath: String? = null) {
        if (savedPath != null) {
            lastRecordedVideoPath = savedPath
        }
        _liveCoachState.update { it.copy(isRecording = recording) }
    }

    fun onPoseFrameAnalyzed(result: BoxingBiomechanicsEngine.FrameAnalysisResult) {
        val st = _liveCoachState.value
        val pose = result.livePoseState

        if (st.sessionActive && !st.isRestPhase && pose.hasPerson) {
            totalPoseFrames++
            if (pose.guardOk) guardGoodFrames++
            footworkScoreSum += pose.footworkScore

            if (st.isRecording && poseTimelineBuffer.size < 1800) {
                val relMs = System.currentTimeMillis() - sessionStartMs
                poseTimelineBuffer.add(
                    PoseFrameSnapshot(
                        timeMs = relMs,
                        joints = pose.normalizedJoints,
                        isGoodForm = pose.guardOk && pose.chinTucked,
                        label = result.newPunchEvent?.punchType ?: ""
                    )
                )
            }
        }

        var updatedStep = st.comboStepIndex
        var updatedCombos = st.combosCompleted
        var bannerHi = st.liveBannerHi
        var bannerEn = st.liveBannerEn

        val punch = result.newPunchEvent
        if (punch != null && st.sessionActive && !st.isRestPhase) {
            bannerHi = "${punch.punchType}: ${punch.score}/100 — ${punch.messageHi}"
            bannerEn = "${punch.punchType}: ${punch.score}/100 — ${punch.messageEn}"

            if (st.comboMode) {
                val seq = st.selectedCombo.sequence
                val expected = seq.getOrNull(updatedStep)
                if (expected == punch.punchType) {
                    updatedStep++
                    if (updatedStep >= seq.size) {
                        updatedStep = 0
                        updatedCombos++
                        val msg = if (prefsState.value.language == AppLanguage.HINGLISH)
                            "Combo complete! Shabash! Dobara: ${st.selectedCombo.voiceCallHi}"
                        else "Combo complete! Nice! Again: ${st.selectedCombo.voiceCallEn}"
                        if (prefsState.value.voiceEnabled) voiceCoach.speakFeedback(msg, force = true)
                    }
                } else {
                    updatedStep = 0
                }
            } else if (prefsState.value.voiceEnabled && !punch.isSahi) {
                val msg = if (prefsState.value.language == AppLanguage.HINGLISH) punch.messageHi else punch.messageEn
                voiceCoach.speakFeedback(msg)
            }
        } else if (result.retractionWarningHi != null && st.sessionActive && !st.isRestPhase) {
            bannerHi = result.retractionWarningHi
            bannerEn = result.retractionWarningEn ?: bannerEn
            if (prefsState.value.voiceEnabled) {
                val msg = if (prefsState.value.language == AppLanguage.HINGLISH)
                    result.retractionWarningHi else result.retractionWarningEn.orEmpty()
                voiceCoach.speakFeedback(msg)
            }
        }

        _liveCoachState.update { current ->
            current.copy(
                livePose = pose,
                lastPunch = punch ?: current.lastPunch,
                liveBannerHi = bannerHi,
                liveBannerEn = bannerEn,
                comboStepIndex = updatedStep,
                combosCompleted = updatedCombos,
                sessionPunches = if (punch != null && current.sessionActive && !current.isRestPhase)
                    current.sessionPunches + punch else current.sessionPunches
            )
        }
    }

    fun endLiveSession() {
        timerJob?.cancel()
        val st = _liveCoachState.value
        val punches = st.sessionPunches
        val durationSec = ((System.currentTimeMillis() - sessionStartMs) / 1000L).toInt().coerceAtLeast(1)
        val avgScore = if (punches.isNotEmpty()) punches.map { it.score }.average().toInt() else 80
        val guardPct = if (totalPoseFrames > 0) ((guardGoodFrames * 100f) / totalPoseFrames).toInt() else 85
        val footworkPct = if (totalPoseFrames > 0) (footworkScoreSum / totalPoseFrames) else 82

        val isHi = prefsState.value.language == AppLanguage.HINGLISH
        val mistakes = punches.filter { !it.isSahi }
            .map { if (isHi) it.messageHi else it.messageEn }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { "${it.key} (${it.value}x)" }

        val best = punches.maxByOrNull { it.score }
        val worst = punches.minByOrNull { it.score }
        val bestStr = best?.let { "${it.punchType} (${it.score})" } ?: "JAB (85)"
        val worstStr = worst?.let { "${it.punchType} (${it.score})" } ?: "-"

        val xpEarned = (30 + punches.size * 3 + (avgScore / 5)).coerceIn(30, 350)
        val jabsCount = punches.count { it.punchType == "JAB" }
        val timelineJson = if (poseTimelineBuffer.isNotEmpty()) json.encodeToString(poseTimelineBuffer.toList()) else null

        viewModelScope.launch {
            val leveledUp = prefsManager.addXpAndUpdateProgress(
                earnedXp = xpEarned,
                punchesInSession = punches.size,
                jabsCount = jabsCount,
                roundsCompleted = st.currentRound,
                guardPct = guardPct
            )
            repo.saveSession(
                SessionRecordEntity(
                    durationSec = durationSec,
                    totalPunches = punches.size,
                    avgScore = avgScore,
                    guardDisciplinePct = guardPct,
                    footworkScorePct = footworkPct,
                    bestPunchType = best?.punchType ?: "JAB",
                    bestPunchScore = best?.score ?: 85,
                    worstPunchType = worst?.punchType ?: "-",
                    worstPunchScore = worst?.score ?: 0,
                    commonMistakesJson = json.encodeToString(mistakes),
                    comboName = if (st.comboMode) st.selectedCombo.code else "Free Shadow",
                    videoPath = lastRecordedVideoPath,
                    poseTimelineJson = timelineJson
                )
            )
            _liveCoachState.update {
                it.copy(
                    sessionActive = false,
                    timerRunning = false,
                    summary = SessionSummaryUi(
                        totalPunches = punches.size,
                        avgScore = avgScore,
                        guardPct = guardPct,
                        footworkPct = footworkPct,
                        bestPunch = bestStr,
                        worstPunch = worstStr,
                        topMistakes = mistakes,
                        xpEarned = xpEarned,
                        leveledUpTo = leveledUp,
                        videoPath = lastRecordedVideoPath,
                        poseTimelineJson = timelineJson
                    )
                )
            }
        }
    }

    fun dismissSessionSummary() {
        _liveCoachState.update { it.copy(summary = null) }
    }

    // --- Schedule Editing ---
    fun toggleDayRest(day: ScheduleDayEntity) {
        viewModelScope.launch {
            repo.upsertScheduleDay(day.copy(isRestDay = !day.isRestDay, isRecoveryDay = false))
        }
    }

    fun toggleDayRecovery(day: ScheduleDayEntity) {
        viewModelScope.launch {
            repo.upsertScheduleDay(day.copy(isRecoveryDay = !day.isRecoveryDay, isRestDay = false))
        }
    }

    fun updateDayTitleAndTime(day: ScheduleDayEntity, newTitle: String, hour: Int, minute: Int, reminderEnabled: Boolean) {
        viewModelScope.launch {
            val updated = day.copy(
                dayTitle = newTitle,
                reminderHour = hour,
                reminderMinute = minute,
                reminderEnabled = reminderEnabled
            )
            repo.upsertScheduleDay(updated)
            ReminderScheduler.scheduleDayReminder(
                context = getApplication(),
                dayOfWeek = updated.dayOfWeek,
                hour = hour,
                minute = minute,
                title = newTitle,
                enabled = reminderEnabled && !updated.isRestDay
            )
        }
    }

    fun addExerciseToDay(day: ScheduleDayEntity, name: String, sets: Int, reps: String, equipment: String) {
        viewModelScope.launch {
            val list = repo.decodeExercises(day.exercisesJson).toMutableList()
            list.add(ScheduledExercise(UUID.randomUUID().toString(), name, sets, reps, equipment))
            repo.upsertScheduleDay(day.copy(exercisesJson = repo.encodeExercises(list)))
        }
    }

    fun updateExerciseInDay(day: ScheduleDayEntity, updatedEx: ScheduledExercise) {
        viewModelScope.launch {
            val list = repo.decodeExercises(day.exercisesJson).map {
                if (it.id == updatedEx.id) updatedEx else it
            }
            repo.upsertScheduleDay(day.copy(exercisesJson = repo.encodeExercises(list)))
        }
    }

    fun deleteExerciseFromDay(day: ScheduleDayEntity, exerciseId: String) {
        viewModelScope.launch {
            val list = repo.decodeExercises(day.exercisesJson).filterNot { it.id == exerciseId }
            repo.upsertScheduleDay(day.copy(exercisesJson = repo.encodeExercises(list)))
        }
    }

    fun moveExerciseInDay(day: ScheduleDayEntity, index: Int, direction: Int) {
        viewModelScope.launch {
            val list = repo.decodeExercises(day.exercisesJson).toMutableList()
            val target = index + direction
            if (index in list.indices && target in list.indices) {
                val temp = list[index]
                list[index] = list[target]
                list[target] = temp
                repo.upsertScheduleDay(day.copy(exercisesJson = repo.encodeExercises(list)))
            }
        }
    }

    // --- Progress & Reports ---
    fun addProgressPhoto(uriString: String, note: String) {
        viewModelScope.launch {
            repo.addProgressPhoto(uriString, prefsState.value.weightKg, note)
        }
    }

    fun deleteProgressPhoto(id: Long) {
        viewModelScope.launch { repo.deleteProgressPhoto(id) }
    }

    fun computeWeeklyReport(allSessions: List<SessionRecordEntity>): WeeklyReportUi {
        val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val recent = allSessions.filter { it.timestamp >= weekAgo }
        if (recent.isEmpty()) {
            return WeeklyReportUi(
                sessionsThisWeek = 0,
                avgFormScore = 0,
                improvedTextHi = "Is hafte ka pehla session start karo!",
                improvedTextEn = "Complete your first session this week!",
                weakPointHi = "Abhi data kam hai — Live Coach me 2-3 rounds khelo.",
                weakPointEn = "Not enough data yet — log 2-3 Live Coach rounds.",
                nextFocusHi = "1-2 Jab-Cross form aur tight guard par focus karo.",
                nextFocusEn = "Focus on 1-2 Jab-Cross extension and tight guard."
            )
        }
        val avgForm = recent.map { it.avgScore }.average().toInt()
        val avgGuard = recent.map { it.guardDisciplinePct }.average().toInt()
        val avgFoot = recent.map { it.footworkScorePct }.average().toInt()

        val improvedHi = if (avgGuard >= avgFoot)
            "Guard discipline ($avgGuard%) strong raha!" else "Footwork balance ($avgFoot%) me badhiya sudhar hua!"
        val improvedEn = if (avgGuard >= avgFoot)
            "Strong guard discipline ($avgGuard%)!" else "Solid footwork balance ($avgFoot%)!"

        val weakHi = if (avgGuard < avgFoot)
            "Punch ke baad non-punching haath kabhi-kabhi neeche gir raha ($avgGuard%)."
        else "Footwork bounce aur stance width thoda aur tight ho sakta hai ($avgFoot%)."

        val weakEn = if (avgGuard < avgFoot)
            "Non-punching guard hand occasionally drops ($avgGuard%)."
        else "Footwork bounce & stance width need tightening ($avgFoot%)."

        val focusHi = if (avgGuard < 80) "Agla Focus: Har jab/cross par opposite hand cheek pe chipkao."
        else "Agla Focus: 1-2-3-2 combinations speed aur hip pivot ke sath."
        val focusEn = if (avgGuard < 80) "Next Focus: Glue rear guard to cheek on every jab/cross."
        else "Next Focus: Speed up 1-2-3-2 combos with sharp hip rotation."

        return WeeklyReportUi(
            sessionsThisWeek = recent.size,
            avgFormScore = avgForm,
            improvedTextHi = improvedHi,
            improvedTextEn = improvedEn,
            weakPointHi = weakHi,
            weakPointEn = weakEn,
            nextFocusHi = focusHi,
            nextFocusEn = focusEn
        )
    }

    suspend fun exportJsonString(): String = repo.exportBackupJson()

    fun importJsonString(raw: String) {
        viewModelScope.launch {
            val ok = repo.importBackupJson(raw)
            _statusToast.value = if (ok) "Backup imported!" else "Invalid backup JSON"
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repo.resetAllData()
            _statusToast.value = "All data reset!"
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        voiceCoach.release()
    }
}
