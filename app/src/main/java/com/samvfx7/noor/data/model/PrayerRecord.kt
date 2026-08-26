package com.samvfx7.noor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PrayerStatus(val label: String, val score: Int) {
    ON_TIME("On Time", 3),
    IN_CONGREGATION("Jama'ah", 4),
    LATE("Late", 2),
    QADA("Qada (Made up)", 1),
    MISSED("Missed", 0);

    val isCompleted: Boolean
        get() = this == ON_TIME || this == IN_CONGREGATION || this == LATE || this == QADA
}

@Entity(tableName = "prayer_records")
data class PrayerRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String, // Format "yyyy-MM-dd"
    val prayerName: String, // FAJR, DHUHR, ASR, MAGHRIB, ISHA, or SUNNAH_*
    val status: String, // PrayerStatus name
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class DayCompletionStat(
    val dateKey: String,
    val dayName: String, // "Mon", "Tue", etc.
    val dayNumber: String, // "25", "26", etc.
    val completedCount: Int, // 0 to 5
    val isToday: Boolean = false,
    val isAllCompleted: Boolean = false
)

data class PrayerStreakStats(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val totalPrayersRecorded: Int = 0,
    val onTimePercentage: Float = 100f,
    val qadaCountPending: Int = 0,
    val weeklyHistory: List<DayCompletionStat> = emptyList(),
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 5
)

