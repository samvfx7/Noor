package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DhikrCategory(val title: String, val subtitle: String) {
    MORNING("Morning Adhkar", "Adhkar as-Sabah (Recited after Fajr until sunrise)"),
    EVENING("Evening Adhkar", "Adhkar al-Masaa (Recited after Asr until sunset)"),
    AFTER_PRAYER("After Prayer", "Post-Salah Adhkar prescribed by the Prophet ﷺ"),
    GENERAL("General Dhikr", "Remembrance of Allah anytime throughout the day"),
    SLEEP("Sleep Adhkar", "Prescribed remembrances before going to bed"),
    FAVORITES("Favorites", "Your pinned daily remembrances")
}

@Entity(tableName = "custom_dhikr")
data class DhikrItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val category: DhikrCategory,
    val reference: String,
    val benefit: String = "",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)

@Entity(tableName = "tasbih_sessions")
data class TasbihSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phraseArabic: String,
    val phraseEnglish: String,
    val totalCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
