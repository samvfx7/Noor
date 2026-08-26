package com.samvfx7.noor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String, // "Meccan" or "Medinan"
    val startJuz: Int,
    val sampleAyahs: List<Ayah> = emptyList()
)

data class Ayah(
    val numberInSurah: Int,
    val numberInQuran: Int,
    val arabicText: String,
    val translationEnglish: String,
    val transliteration: String = "",
    val juz: Int = 1
)

data class JuzInfo(
    val juzNumber: Int,
    val nameArabic: String,
    val startSurahNumber: Int,
    val startSurahName: String,
    val startAyahNumber: Int
)

@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val snippet: String,
    val createdAt: Long = System.currentTimeMillis()
)
