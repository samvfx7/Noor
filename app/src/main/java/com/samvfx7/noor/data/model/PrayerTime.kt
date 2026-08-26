package com.samvfx7.noor.data.model

enum class PrayerName(val displayName: String, val arabicName: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    SUNSET("Sunset", "الغروب"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء"),
    MIDNIGHT("Midnight", "منتصف الليل"),
    QIYAM("Qiyam", "قيام الليل")
}

enum class CalculationMethod(
    val title: String,
    val description: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int = 0
) {
    MWL("Muslim World League", "Fajr 18°, Isha 17° (Europe, Far East, parts of US)", 18.0, 17.0),
    ISNA("Islamic Society of North America", "Fajr 15°, Isha 15° (North America)", 15.0, 15.0),
    EGYPT("Egyptian General Authority", "Fajr 19.5°, Isha 17.5° (Africa, Syria, Lebanon)", 19.5, 17.5),
    MAKKAH("Umm Al-Qura University", "Fajr 18.5°, Isha 90 min after Maghrib (Arabian Peninsula)", 18.5, 0.0, 90),
    KARACHI("Univ. of Islamic Sciences, Karachi", "Fajr 18°, Isha 18° (Pakistan, India, Bangladesh)", 18.0, 18.0),
    TEHRAN("Institute of Geophysics, Tehran", "Fajr 17.7°, Isha 14°", 17.7, 14.0),
    GULF("Gulf / Dubai Region", "Fajr 18.2°, Isha 18.2°", 18.2, 18.2),
    FRANCE("UOIF (France)", "Fajr 12°, Isha 12° (High latitudes)", 12.0, 12.0),
    SINGAPORE("MUIS (Singapore)", "Fajr 20°, Isha 18° (Southeast Asia)", 20.0, 18.0)
}

enum class Madhhab(val title: String, val shadowFactor: Double) {
    SHAFI("Standard (Shafi'i, Maliki, Hanbali)", 1.0),
    HANAFI("Hanafi", 2.0)
}

enum class HighLatitudeRule(val title: String) {
    MIDDLE_OF_NIGHT("Middle of the Night"),
    SEVENTH_OF_NIGHT("One-Seventh of the Night"),
    TWILIGHT_ANGLE("Angle-Based Rule")
}

data class CityLocation(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String
)

data class DayPrayerSchedule(
    val dateEpochDays: Long,
    val dateFormatted: String,
    val fajrMillis: Long,
    val sunriseMillis: Long,
    val dhuhrMillis: Long,
    val asrMillis: Long,
    val sunsetMillis: Long,
    val maghribMillis: Long,
    val ishaMillis: Long,
    val midnightMillis: Long,
    val qiyamMillis: Long,
    val locationName: String
) {
    fun getPrayerTime(prayer: PrayerName): Long {
        return when (prayer) {
            PrayerName.FAJR -> fajrMillis
            PrayerName.SUNRISE -> sunriseMillis
            PrayerName.DHUHR -> dhuhrMillis
            PrayerName.ASR -> asrMillis
            PrayerName.SUNSET -> sunsetMillis
            PrayerName.MAGHRIB -> maghribMillis
            PrayerName.ISHA -> ishaMillis
            PrayerName.MIDNIGHT -> midnightMillis
            PrayerName.QIYAM -> qiyamMillis
        }
    }
}

data class NextPrayerInfo(
    val currentPrayer: PrayerName,
    val nextPrayer: PrayerName,
    val nextPrayerTimeMillis: Long,
    val remainingMillis: Long,
    val progressFraction: Float
)
