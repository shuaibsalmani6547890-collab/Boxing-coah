package com.example.data.model

import kotlinx.serialization.Serializable

enum class AppLanguage {
    HINGLISH,
    ENGLISH
}

enum class AppThemeStyle(val labelHi: String, val labelEn: String) {
    CRIMSON_ARENA("Crimson Fight Night", "Crimson Fight Night"),
    HUNTER_BLUE("Shadow Hunter Blue", "Shadow Hunter Blue"),
    VIPER_EMERALD("Viper Neon Green", "Viper Neon Green")
}

enum class HunterRank(val letter: String, val minLevel: Int) {
    E("E", 1),
    D("D", 3),
    C("C", 6),
    B("B", 10),
    A("A", 15),
    S("S", 22);

    companion object {
        fun fromLevel(level: Int): HunterRank =
            entries.lastOrNull { level >= it.minLevel } ?: E
    }
}

enum class PunchType(val code: String, val labelHi: String, val labelEn: String) {
    JAB("1", "Jab (1)", "Jab (1)"),
    CROSS("2", "Cross (2)", "Cross (2)"),
    LEAD_HOOK("3", "Hook (3)", "Lead Hook (3)"),
    UPPERCUT("5", "Uppercut (5)", "Uppercut (5)")
}

enum class TrainingGoal(val labelHi: String, val labelEn: String) {
    FAT_LOSS("Fat Loss", "Fat Loss"),
    STAMINA("Stamina", "Stamina"),
    STRENGTH("Strength", "Strength"),
    SKILL("Boxing Skill", "Boxing Skill")
}

enum class Equipment(val labelHi: String, val labelEn: String) {
    NONE("Kuch Nahi (Bodyweight)", "No Equipment"),
    SKIPPING_ROPE("Skipping Rope", "Skipping Rope"),
    DUMBBELL("Dumbbell", "Dumbbells"),
    HEAVY_BAG("Heavy Bag", "Heavy Bag"),
    BENCH("Bench", "Bench"),
    PULL_UP_BAR("Pull-up Bar", "Pull-up Bar"),
    MEDICINE_BALL("Medicine Ball", "Medicine Ball")
}

enum class SchedulePlanType(val labelHi: String, val labelEn: String) {
    BEGINNER("Beginner (3 Box + 3 Gym + 1 Rest)", "Beginner (3 Box + 3 Gym + 1 Rest)"),
    INTERMEDIATE("Intermediate (4 Box + 2 Gym + 1 Recovery)", "Intermediate (4 Box + 2 Gym + 1 Recovery)"),
    CUSTOM("Custom Plan", "Custom Plan")
}

enum class TechniqueAnimType {
    STANCE_ORTHODOX,
    STANCE_SOUTHPAW,
    JAB,
    CROSS,
    HOOK,
    UPPERCUT,
    SLIP,
    BOB_WEAVE,
    PARRY,
    STEP_DRAG,
    PIVOT,
    CIRCLE_STEP
}

enum class ExerciseCategory(val labelHi: String, val labelEn: String, val descHi: String, val descEn: String) {
    RING_BAG(
        "Ring & Bag Drills",
        "Ring & Bag Drills",
        "Speed, rhythm, punching power aur reflex drills",
        "Speed, rhythm, punching power & reflex drills"
    ),
    CARDIO_CONDITIONING(
        "Cardio & Stamina",
        "Cardio & Conditioning",
        "12-round gas tank aur footwork agility",
        "12-round gas tank & footwork agility"
    ),
    UPPER_POWER(
        "Upper Body & Core",
        "Upper Body & Core",
        "Punching snap, rotational torque aur rib armor",
        "Punching snap, rotational torque & rib armor"
    ),
    LEGS_BULLETPROOF(
        "Legs & Injury Proof",
        "Legs, Neck & Wrists",
        "Knockout base power, strong chin aur wrist safety",
        "Knockout leg drive, strong chin & wrist safety"
    )
}

enum class ExerciseMotionType {
    SHADOW_BOXING,
    HEAVY_BAG,
    SPEED_BAG,
    DOUBLE_END_BAG,
    SKIPPING,
    BURPEES,
    RUNNING_SPRINTS,
    MOUNTAIN_CLIMBERS,
    AGILITY_FOOTWORK,
    PUSH_UPS,
    PULL_UPS,
    MED_BALL_SLAM,
    PLANK_TAPS,
    RUSSIAN_TWISTS,
    SQUATS,
    LUNGES,
    DEADLIFT,
    NECK_ISOMETRIC,
    WRIST_CURLS
}

data class BoxingTechnique(
    val id: String,
    val nameHi: String,
    val nameEn: String,
    val categoryHi: String,
    val categoryEn: String,
    val animType: TechniqueAnimType,
    val stepsHi: List<String>,
    val stepsEn: List<String>,
    val mistakesHi: List<String>,
    val mistakesEn: List<String>
)

data class BoxingCombo(
    val id: String,
    val code: String,
    val nameHi: String,
    val nameEn: String,
    val sequence: List<String>,
    val voiceCallHi: String,
    val voiceCallEn: String,
    val difficulty: String
)

data class BoxingExercise(
    val id: String,
    val category: ExerciseCategory,
    val motionType: ExerciseMotionType,
    val nameHi: String,
    val nameEn: String,
    val muscleHi: String,
    val muscleEn: String,
    val howToHi: String,
    val howToEn: String,
    val stepsHi: List<String>,
    val stepsEn: List<String>,
    val mistakesHi: List<String>,
    val mistakesEn: List<String>,
    val boxingBenefitHi: String,
    val boxingBenefitEn: String,
    val tempoCue: String,
    val restSeconds: Int,
    val baseSets: Int,
    val baseReps: String,
    val equipment: Equipment,
    val baseWeightFactor: Float = 0f
)

data class IndianFoodItem(
    val nameHi: String,
    val nameEn: String,
    val servingHi: String,
    val servingEn: String,
    val calories: Int,
    val proteinG: Int
)

@Serializable
data class ScheduledExercise(
    val id: String,
    val name: String,
    val sets: Int,
    val reps: String,
    val equipment: String = "Bodyweight"
)

@Serializable
data class PosePointNorm(
    val x: Float,
    val y: Float
)

@Serializable
data class PoseFrameSnapshot(
    val timeMs: Long,
    val joints: Map<Int, PosePointNorm>,
    val isGoodForm: Boolean,
    val label: String = ""
)

@Serializable
data class PunchFeedbackEvent(
    val punchType: String,
    val score: Int,
    val isSahi: Boolean,
    val messageHi: String,
    val messageEn: String,
    val timestampMs: Long
)

data class DailyQuest(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val target: Int,
    val current: Int,
    val xpReward: Int,
    val claimed: Boolean
)

data class HunterBadge(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val descHi: String,
    val descEn: String,
    val unlocked: Boolean
)

@Serializable
data class FullBackupPayload(
    val weightKg: Float,
    val goal: String,
    val xp: Int,
    val level: Int,
    val streak: Int,
    val longestStreak: Int,
    val mostPunchesOneRound: Int,
    val language: String,
    val activePlan: String
)
