package com.samvfx7.noor.data.model

enum class DuaCategory(val title: String, val iconName: String) {
    MORNING_EVENING("Morning & Evening", "wb_sunny"),
    SLEEP_WAKE("Sleep & Waking", "bedtime"),
    PRAYER_MOSQUE("Prayer & Mosque", "mosque"),
    FOOD_DRINK("Food & Drink", "restaurant"),
    TRAVEL("Travel & Journey", "directions_car"),
    PROTECTION("Protection & Refuge", "shield"),
    FORGIVENESS("Forgiveness & Repentance", "favorite"),
    ILLNESS_HARDSHIP("Illness & Hardship", "healing"),
    PRAISE_THANKS("Praise & Gratitude", "thumb_up"),
    RAMADAN_FASTING("Fasting & Ramadan", "nightlight")
}

data class DuaItem(
    val id: String,
    val title: String,
    val category: DuaCategory,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val reference: String,
    val whenToRead: String = "",
    val isFavorite: Boolean = false
)
