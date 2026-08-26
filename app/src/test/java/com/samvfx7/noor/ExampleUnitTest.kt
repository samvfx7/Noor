package com.samvfx7.noor

import com.samvfx7.noor.data.model.ChallengeDifficulty
import com.samvfx7.noor.data.model.DayCompletionStat
import com.samvfx7.noor.data.model.FajrAlarmConfig
import com.samvfx7.noor.data.model.FajrChallengeType
import com.samvfx7.noor.data.model.PrayerMatProfile
import com.samvfx7.noor.data.model.PrayerStatus
import com.samvfx7.noor.data.model.PrayerStreakStats
import com.samvfx7.noor.service.PrayerMatRecognitionEngine
import com.samvfx7.noor.ui.screens.FajrRoutineStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPrayerMatSignatureMatchingExact() {
        val cell1 = PrayerMatRecognitionEngine.SpatialCell(
            hueHistogram = FloatArray(12) { if (it == 2) 1.0f else 0.0f },
            avgSaturation = 0.8f,
            avgBrightness = 0.7f,
            edgeDensity = 0.3f
        )
        val sig1 = PrayerMatRecognitionEngine.PrayerMatSignature(
            id = "test-1",
            cells = listOf(cell1),
            dominantHueBins = intArrayOf(2, 3, 0, 1),
            luminanceMatrix = FloatArray(64) { 0.5f }
        )
        val sig2 = PrayerMatRecognitionEngine.PrayerMatSignature(
            id = "test-2",
            cells = listOf(cell1),
            dominantHueBins = intArrayOf(2, 3, 0, 1),
            luminanceMatrix = FloatArray(64) { 0.5f }
        )

        val confidence = PrayerMatRecognitionEngine.calculateMatchConfidence(sig1, listOf(sig2))
        assertTrue("Matching identical signatures should have high confidence (> 0.9)", confidence > 0.9f)
    }

    @Test
    fun testFajrRoutineStagesOrder() {
        val stages = FajrRoutineStage.values()
        assertEquals(4, stages.size)
        assertEquals(FajrRoutineStage.WAKE, stages[0])
        assertEquals(FajrRoutineStage.WUDU, stages[1])
        assertEquals(FajrRoutineStage.PRAYER_MAT, stages[2])
        assertEquals(FajrRoutineStage.PRAY, stages[3])
    }

    @Test
    fun testFajrAlarmConfigDefaults() {
        val config = FajrAlarmConfig()
        assertTrue(config.isEnabled)
        assertEquals(5, config.snoozeDurationMinutes)
        assertEquals(2, config.maxSnoozes)
        assertEquals(5, config.wuduTimerMinutes)
        assertTrue(config.isWuduGuideEnabled)
        assertEquals(FajrChallengeType.PRAYER_MAT_RECOGNITION, config.challengeType)
    }

    @Test
    fun testPrayerStatusCompletion() {
        assertTrue(PrayerStatus.ON_TIME.isCompleted)
        assertTrue(PrayerStatus.IN_CONGREGATION.isCompleted)
        assertTrue(PrayerStatus.LATE.isCompleted)
        assertTrue(PrayerStatus.QADA.isCompleted)
        assertFalse(PrayerStatus.MISSED.isCompleted)
    }

    @Test
    fun testPrayerStreakStatsDefaults() {
        val stats = PrayerStreakStats()
        assertEquals(0, stats.currentStreakDays)
        assertEquals(0, stats.longestStreakDays)
        assertEquals(5, stats.todayTotalCount)
        assertEquals(0, stats.todayCompletedCount)
    }

    @Test
    fun testDayCompletionStat() {
        val stat = DayCompletionStat(
            dateKey = "2026-08-25",
            dayName = "Tue",
            dayNumber = "25",
            completedCount = 5,
            isToday = true,
            isAllCompleted = true
        )
        assertTrue(stat.isAllCompleted)
        assertEquals(5, stat.completedCount)
    }
}
