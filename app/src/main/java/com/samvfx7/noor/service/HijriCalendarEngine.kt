package com.samvfx7.noor.service

import com.samvfx7.noor.data.model.HijriDate
import com.samvfx7.noor.data.model.IslamicEvent
import java.util.Calendar
import kotlin.math.floor

object HijriCalendarEngine {

    private val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val HIJRI_MONTHS_AR = listOf(
        "محرّم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    fun getHijriDate(gregorianCal: Calendar = Calendar.getInstance(), dayAdjustment: Int = 0): HijriDate {
        val y = gregorianCal.get(Calendar.YEAR)
        val m = gregorianCal.get(Calendar.MONTH) + 1
        val d = gregorianCal.get(Calendar.DAY_OF_MONTH)

        // Kuwaity algorithm / Julian Day to Hijri conversion
        var julianDay = julianDayNumber(y, m, d) + dayAdjustment

        var l = julianDay - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        l = l - 10631 * n + 354
        val j = (floor((10985 - l) / 5316.0) * floor((50 * l + 17719) / 17719.0) +
                floor(l / 5670.0) * floor((43 * l + 15238) / 15238.0)).toInt()
        l = l - floor((30 - j) / 15.0).toInt() * floor((17719 * j) / 50.0).toInt() -
                floor(j / 16.0).toInt() * floor((15238 * j) / 43.0).toInt() + 29
        val month = floor((24 * l) / 709.0).toInt()
        val day = (l - floor((709 * month) / 24.0)).toInt()
        val year = (30 * n + j - 30)

        val monthIndex = (month - 1).coerceIn(0, 11)
        val monthEn = HIJRI_MONTHS_EN[monthIndex]
        val monthAr = HIJRI_MONTHS_AR[monthIndex]

        return HijriDate(
            day = day,
            monthNumber = month,
            monthNameArabic = monthAr,
            monthNameEnglish = monthEn,
            year = year,
            formatted = "$day $monthEn $year AH"
        )
    }

    private fun julianDayNumber(year: Int, month: Int, day: Int): Int {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0).toInt()
        val b = 2 - a + floor(a / 4.0).toInt()
        return floor(365.25 * (y + 4716)).toInt() + floor(30.6001 * (m + 1)).toInt() + day + b - 1524
    }

    val ISLAMIC_EVENTS = listOf(
        IslamicEvent("Islamic New Year", "رأس السنة الهجرية", 1, 1, "Muharram", "Beginning of the Hijri Calendar", true),
        IslamicEvent("Day of Ashura", "يوم عاشوراء", 10, 1, "Muharram", "Fasting of Ashura", false),
        IslamicEvent("Mawlid an-Nabi", "المولد النبوي", 12, 3, "Rabi' al-Awwal", "Birth of Prophet Muhammad ﷺ", false),
        IslamicEvent("Isra and Mi'raj", "الإسراء والمعراج", 27, 7, "Rajab", "The Night Journey and Ascension", false),
        IslamicEvent("Mid-Sha'ban (Nisf Sha'ban)", "ليلة النصف من شعبان", 15, 8, "Sha'ban", "Night of forgiveness and preparation for Ramadan", false),
        IslamicEvent("First Day of Ramadan", "أول أيام رمضان", 1, 9, "Ramadan", "Start of the holy month of fasting", true),
        IslamicEvent("Laylat al-Qadr (Estimated)", "ليلة القدر", 27, 9, "Ramadan", "The Night of Decree and Power", true),
        IslamicEvent("Eid al-Fitr", "عيد الفطر المبارك", 1, 10, "Shawwal", "Celebration of the conclusion of Ramadan", true),
        IslamicEvent("Day of Arafah", "يوم عرفة", 9, 12, "Dhu al-Hijjah", "The peak of the Hajj pilgrimage", true),
        IslamicEvent("Eid al-Adha", "عيد الأضحى المبارك", 10, 12, "Dhu al-Hijjah", "Festival of Sacrifice", true)
    )
}
