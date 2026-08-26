package com.samvfx7.noor.service

import com.samvfx7.noor.data.model.CalculationMethod
import com.samvfx7.noor.data.model.CityLocation
import com.samvfx7.noor.data.model.DayPrayerSchedule
import com.samvfx7.noor.data.model.HighLatitudeRule
import com.samvfx7.noor.data.model.Madhhab
import com.samvfx7.noor.data.model.NextPrayerInfo
import com.samvfx7.noor.data.model.PrayerName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerCalculationEngine {

    val DEFAULT_CITIES = listOf(
        CityLocation("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
        CityLocation("Madinah", "Saudi Arabia", 24.4672, 39.6111, "Asia/Riyadh"),
        CityLocation("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
        CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
        CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
        CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
        CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
        CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
        CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityLocation("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
        CityLocation("Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
        CityLocation("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
        CityLocation("Singapore", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
        CityLocation("Amman", "Jordan", 31.9454, 35.9284, "Asia/Amman"),
        CityLocation("Doha", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
        CityLocation("Algiers", "Algeria", 36.7538, 3.0588, "Africa/Algiers"),
        CityLocation("Rabat", "Morocco", 34.0209, -6.8416, "Africa/Casablanca"),
        CityLocation("Chicago", "United States", 41.8781, -87.6298, "America/Chicago"),
        CityLocation("Los Angeles", "United States", 34.0522, -118.2437, "America/Los_Angeles")
    )

    fun calculateTimes(
        year: Int,
        month: Int, // 1-indexed (1-12)
        day: Int,
        latitude: Double,
        longitude: Double,
        timezone: Double,
        method: CalculationMethod = CalculationMethod.MWL,
        madhhab: Madhhab = Madhhab.SHAFI,
        highLatRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
        manualOffsetsMinutes: Map<PrayerName, Int> = emptyMap(),
        locationName: String = "Current Location"
    ): DayPrayerSchedule {
        val julianDay = julianDay(year, month, day) - longitude / (15.0 * 24.0)

        // Sun position at midday
        val sunMidday = sunPosition(julianDay)
        val equationOfTime = sunMidday.equationOfTime
        val declination = sunMidday.declination

        // Solar noon (Dhuhr) in fractional hours
        val noon = fixHour(12.0 + timezone - longitude / 15.0 - equationOfTime)

        // Sunrise & Sunset angle is 0.833 degrees
        val sunAngle = 0.8333333
        val sunriseHourAngle = hourAngle(latitude, declination, -sunAngle)
        val sunrise = if (sunriseHourAngle.isNaN()) noon - 6.0 else noon - sunriseHourAngle / 15.0
        val sunset = if (sunriseHourAngle.isNaN()) noon + 6.0 else noon + sunriseHourAngle / 15.0

        // Fajr calculation
        val fajrHourAngle = hourAngle(latitude, declination, -method.fajrAngle)
        var fajr = if (fajrHourAngle.isNaN()) {
            adjustHighLatitude(noon, sunrise, sunset, method.fajrAngle, isFajr = true, rule = highLatRule)
        } else {
            noon - fajrHourAngle / 15.0
        }

        // Asr calculation
        val asrFactor = madhhab.shadowFactor
        val asrAngle = -toDeg(atan(1.0 / (asrFactor + tan(toRad(abs(latitude - declination))))))
        val asrHourAngle = hourAngle(latitude, declination, asrAngle)
        val asr = if (asrHourAngle.isNaN()) noon + 3.0 else noon + asrHourAngle / 15.0

        // Maghrib calculation (usually at sunset, or fixed angle in Tehran/some methods)
        val maghrib = sunset

        // Isha calculation
        var isha = if (method.ishaIntervalMinutes > 0) {
            maghrib + method.ishaIntervalMinutes / 60.0
        } else {
            val ishaHourAngle = hourAngle(latitude, declination, -method.ishaAngle)
            if (ishaHourAngle.isNaN()) {
                adjustHighLatitude(noon, sunrise, sunset, method.ishaAngle, isFajr = false, rule = highLatRule)
            } else {
                noon + ishaHourAngle / 15.0
            }
        }

        // Night calculations (Midnight & Qiyam)
        val nextDayFajr = fajr + 24.0
        val nightDuration = nextDayFajr - sunset
        val midnight = fixHour(sunset + nightDuration / 2.0)
        val qiyam = fixHour(sunset + (nightDuration * 2.0 / 3.0))

        // Convert fractional hours into Epoch Milliseconds for the given date
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val baseDayMillis = cal.timeInMillis

        fun toMillis(fractionalHour: Double, prayer: PrayerName): Long {
            val hours = fractionalHour.toInt()
            val minutes = ((fractionalHour - hours) * 60).toInt()
            val seconds = ((((fractionalHour - hours) * 60) - minutes) * 60).toInt()
            val prayerCal = Calendar.getInstance().apply {
                timeInMillis = baseDayMillis
                set(Calendar.HOUR_OF_DAY, hours % 24)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, seconds)
                val offset = manualOffsetsMinutes[prayer] ?: 0
                add(Calendar.MINUTE, offset)
            }
            return prayerCal.timeInMillis
        }

        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

        return DayPrayerSchedule(
            dateEpochDays = baseDayMillis / (24 * 60 * 60 * 1000L),
            dateFormatted = dateKey,
            fajrMillis = toMillis(fajr, PrayerName.FAJR),
            sunriseMillis = toMillis(sunrise, PrayerName.SUNRISE),
            dhuhrMillis = toMillis(noon, PrayerName.DHUHR),
            asrMillis = toMillis(asr, PrayerName.ASR),
            sunsetMillis = toMillis(sunset, PrayerName.SUNSET),
            maghribMillis = toMillis(maghrib, PrayerName.MAGHRIB),
            ishaMillis = toMillis(isha, PrayerName.ISHA),
            midnightMillis = toMillis(midnight, PrayerName.MIDNIGHT),
            qiyamMillis = toMillis(qiyam, PrayerName.QIYAM),
            locationName = locationName
        )
    }

    fun getNextPrayer(scheduleToday: DayPrayerSchedule, scheduleTomorrow: DayPrayerSchedule, nowMillis: Long = System.currentTimeMillis()): NextPrayerInfo {
        val prayers = listOf(
            PrayerName.FAJR to scheduleToday.fajrMillis,
            PrayerName.SUNRISE to scheduleToday.sunriseMillis,
            PrayerName.DHUHR to scheduleToday.dhuhrMillis,
            PrayerName.ASR to scheduleToday.asrMillis,
            PrayerName.MAGHRIB to scheduleToday.maghribMillis,
            PrayerName.ISHA to scheduleToday.ishaMillis
        )

        for (i in prayers.indices) {
            val (pName, pTime) = prayers[i]
            if (nowMillis < pTime) {
                val prevPrayerName = if (i == 0) PrayerName.ISHA else prayers[i - 1].first
                val prevPrayerTime = if (i == 0) scheduleToday.fajrMillis - (6 * 3600 * 1000L) else prayers[i - 1].second
                val totalWindow = (pTime - prevPrayerTime).coerceAtLeast(1L)
                val elapsed = (nowMillis - prevPrayerTime).coerceAtLeast(0L)
                val fraction = (elapsed.toFloat() / totalWindow.toFloat()).coerceIn(0f, 1f)

                return NextPrayerInfo(
                    currentPrayer = prevPrayerName,
                    nextPrayer = pName,
                    nextPrayerTimeMillis = pTime,
                    remainingMillis = pTime - nowMillis,
                    progressFraction = fraction
                )
            }
        }

        // All prayers for today have passed, next is tomorrow's Fajr
        val nextFajr = scheduleTomorrow.fajrMillis
        val ishaTime = scheduleToday.ishaMillis
        val totalWindow = (nextFajr - ishaTime).coerceAtLeast(1L)
        val elapsed = (nowMillis - ishaTime).coerceAtLeast(0L)
        val fraction = (elapsed.toFloat() / totalWindow.toFloat()).coerceIn(0f, 1f)

        return NextPrayerInfo(
            currentPrayer = PrayerName.ISHA,
            nextPrayer = PrayerName.FAJR,
            nextPrayerTimeMillis = nextFajr,
            remainingMillis = (nextFajr - nowMillis).coerceAtLeast(0L),
            progressFraction = fraction
        )
    }

    // Trigonometric math helpers
    private fun toRad(deg: Double) = deg * PI / 180.0
    private fun toDeg(rad: Double) = rad * 180.0 / PI
    private fun fixHour(a: Double): Double {
        var h = a - 24.0 * floor(a / 24.0)
        return if (h < 0) h + 24.0 else h
    }
    private fun fixAngle(a: Double): Double {
        var ang = a - 360.0 * floor(a / 360.0)
        return if (ang < 0) ang + 360.0 else ang
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private data class SunCoordinates(val declination: Double, val equationOfTime: Double)

    private fun sunPosition(jd: Double): SunCoordinates {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(toRad(g)) + 0.020 * sin(toRad(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(toDeg(atan2(cos(toRad(e)) * sin(toRad(l)), cos(toRad(l))))) / 15.0
        val declination = toDeg(asin(sin(toRad(e)) * sin(toRad(l))))
        val equationOfTime = q / 15.0 - ra
        return SunCoordinates(declination, equationOfTime)
    }

    private fun hourAngle(lat: Double, decl: Double, angle: Double): Double {
        val cosHA = (sin(toRad(angle)) - sin(toRad(lat)) * sin(toRad(decl))) / (cos(toRad(lat)) * cos(toRad(decl)))
        return if (cosHA < -1.0 || cosHA > 1.0) Double.NaN else toDeg(acos(cosHA))
    }

    private fun adjustHighLatitude(noon: Double, sunrise: Double, sunset: Double, angle: Double, isFajr: Boolean, rule: HighLatitudeRule): Double {
        val nightDuration = fixHour(sunrise - sunset + 24.0)
        val portion = when (rule) {
            HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5
            HighLatitudeRule.SEVENTH_OF_NIGHT -> 1.0 / 7.0
            HighLatitudeRule.TWILIGHT_ANGLE -> angle / 60.0
        }
        val timeDelta = portion * nightDuration
        return if (isFajr) sunrise - timeDelta else sunset + timeDelta
    }

    fun calculateQiblaBearing(userLat: Double, userLon: Double): Double {
        val kaabaLat = 21.4225
        val kaabaLon = 39.8262

        val lat1 = toRad(userLat)
        val lat2 = toRad(kaabaLat)
        val dLon = toRad(kaabaLon - userLon)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = toDeg(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }
}
