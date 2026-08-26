package com.samvfx7.noor.data.model

enum class FajrChallengeType(val title: String, val subtitle: String) {
    PRAYER_MAT_RECOGNITION("Prayer Mat Recognition", "Point live camera at your physical prayer rug"),
    MULTI_STEP("Multi-Step Routine", "Wake up → Wudu → Scan Prayer Mat → Morning Adhkar"),
    MATH("Math Problems", "Solve quick arithmetic to prove alertness"),
    MEMORY("Memory Sequence", "Repeat the pattern sequence accurately"),
    SHAKE("Movement / Shake", "Shake phone briskly 25 times")
}

enum class ChallengeDifficulty(val title: String) {
    EASY("Easy (3 questions / 15 shakes)"),
    MEDIUM("Medium (5 questions / 25 shakes)"),
    HARD("Hard (7 complex questions / 45 shakes)")
}

data class PrayerMatProfile(
    val isRegistered: Boolean = false,
    val registeredTimestamp: Long = 0L,
    val sampleCount: Int = 0,
    val signaturesJson: String = "",
    val matName: String = "My Prayer Rug"
)

data class FajrAlarmConfig(
    val isEnabled: Boolean = true,
    val preAlarmMinutes: Int = 0, // 0 = at Fajr, 10 = 10 min before, etc.
    val challengeType: FajrChallengeType = FajrChallengeType.PRAYER_MAT_RECOGNITION,
    val fallbackChallengeType: FajrChallengeType = FajrChallengeType.MATH,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.MEDIUM,
    val isVibrationEnabled: Boolean = true,
    val volume: Float = 1.0f,
    val alarmToneName: String = "Serene Dawn Adhan",
    val requiredConsecutiveFrames: Int = 6,
    val confidenceThreshold: Float = 0.65f,
    val snoozeDurationMinutes: Int = 5, // 0 = disabled, 5, 10
    val maxSnoozes: Int = 2, // 0 = disabled, 1, 2, 3
    val wuduTimerMinutes: Int = 5, // 3, 5, 7, 10
    val isWuduGuideEnabled: Boolean = true,
    val isWuduGuideDetailed: Boolean = false,
    val prayerMatProfile: PrayerMatProfile = PrayerMatProfile()
)

data class WuduStepItem(
    val stepNumber: Int,
    val title: String,
    val arabicPhrase: String? = null,
    val shortSummary: String,
    val detailedDescription: String,
    val reference: String
)

data class MathProblem(
    val question: String,
    val options: List<Int>,
    val correctIndex: Int
)

