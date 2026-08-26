package com.samvfx7.noor.data.model

data class HijriDate(
    val day: Int,
    val monthNumber: Int,
    val monthNameArabic: String,
    val monthNameEnglish: String,
    val year: Int,
    val formatted: String
)

data class IslamicEvent(
    val name: String,
    val nameArabic: String,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriMonthName: String,
    val description: String,
    val isMajorHoliday: Boolean = false
)
