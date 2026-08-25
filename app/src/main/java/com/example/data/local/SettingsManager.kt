package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CalculationMethod
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.FajrAlarmConfig
import com.example.data.model.FajrChallengeType
import com.example.data.model.HighLatitudeRule
import com.example.data.model.Madhhab
import com.example.data.model.PrayerName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("noor_user_settings", Context.MODE_PRIVATE)

    // Current State Flow for reactive updates
    private val _settingsChangedFlow = MutableStateFlow(System.currentTimeMillis())
    val settingsChangedFlow: StateFlow<Long> = _settingsChangedFlow.asStateFlow()

    private fun notifyChanged() {
        _settingsChangedFlow.value = System.currentTimeMillis()
    }

    var cityName: String
        get() = prefs.getString("city_name", "Makkah") ?: "Makkah"
        set(value) {
            prefs.edit().putString("city_name", value).apply()
            notifyChanged()
        }

    var countryName: String
        get() = prefs.getString("country_name", "Saudi Arabia") ?: "Saudi Arabia"
        set(value) {
            prefs.edit().putString("country_name", value).apply()
            notifyChanged()
        }

    var latitude: Double
        get() = prefs.getFloat("latitude", 21.4225f).toDouble()
        set(value) {
            prefs.edit().putFloat("latitude", value.toFloat()).apply()
            notifyChanged()
        }

    var longitude: Double
        get() = prefs.getFloat("longitude", 39.8262f).toDouble()
        set(value) {
            prefs.edit().putFloat("longitude", value.toFloat()).apply()
            notifyChanged()
        }

    var timezoneOffsetHours: Double
        get() = prefs.getFloat("timezone_offset", 3.0f).toDouble()
        set(value) {
            prefs.edit().putFloat("timezone_offset", value.toFloat()).apply()
            notifyChanged()
        }

    var calculationMethod: CalculationMethod
        get() {
            val name = prefs.getString("calc_method", CalculationMethod.MAKKAH.name) ?: CalculationMethod.MAKKAH.name
            return try { CalculationMethod.valueOf(name) } catch (e: Exception) { CalculationMethod.MAKKAH }
        }
        set(value) {
            prefs.edit().putString("calc_method", value.name).apply()
            notifyChanged()
        }

    var madhhab: Madhhab
        get() {
            val name = prefs.getString("madhhab", Madhhab.SHAFI.name) ?: Madhhab.SHAFI.name
            return try { Madhhab.valueOf(name) } catch (e: Exception) { Madhhab.SHAFI }
        }
        set(value) {
            prefs.edit().putString("madhhab", value.name).apply()
            notifyChanged()
        }

    var highLatitudeRule: HighLatitudeRule
        get() {
            val name = prefs.getString("high_lat_rule", HighLatitudeRule.MIDDLE_OF_NIGHT.name) ?: HighLatitudeRule.MIDDLE_OF_NIGHT.name
            return try { HighLatitudeRule.valueOf(name) } catch (e: Exception) { HighLatitudeRule.MIDDLE_OF_NIGHT }
        }
        set(value) {
            prefs.edit().putString("high_lat_rule", value.name).apply()
            notifyChanged()
        }

    var hijriDayAdjustment: Int
        get() = prefs.getInt("hijri_day_adj", 0)
        set(value) {
            prefs.edit().putInt("hijri_day_adj", value).apply()
            notifyChanged()
        }

    // Per-prayer offsets (in minutes, -30 to +30)
    fun getPrayerOffset(prayer: PrayerName): Int {
        return prefs.getInt("offset_${prayer.name}", 0)
    }

    fun setPrayerOffset(prayer: PrayerName, minutes: Int) {
        prefs.edit().putInt("offset_${prayer.name}", minutes).apply()
        notifyChanged()
    }

    fun getAllPrayerOffsets(): Map<PrayerName, Int> {
        return PrayerName.values().associateWith { getPrayerOffset(it) }
    }

    // Notifications configuration
    fun isPrayerNotificationEnabled(prayer: PrayerName): Boolean {
        return prefs.getBoolean("notif_${prayer.name}", true)
    }

    fun setPrayerNotificationEnabled(prayer: PrayerName, enabled: Boolean) {
        prefs.edit().putBoolean("notif_${prayer.name}", enabled).apply()
        notifyChanged()
    }

    // Advanced Fajr Alarm Config with Prayer Mat Camera Recognition
    fun getPrayerMatProfile(): com.example.data.model.PrayerMatProfile {
        val isReg = prefs.getBoolean("prayer_mat_is_registered", false)
        val time = prefs.getLong("prayer_mat_registered_time", 0L)
        val samples = prefs.getInt("prayer_mat_sample_count", 0)
        val sigs = prefs.getString("prayer_mat_signatures_json", "") ?: ""
        val name = prefs.getString("prayer_mat_name", "My Prayer Rug") ?: "My Prayer Rug"
        return com.example.data.model.PrayerMatProfile(
            isRegistered = isReg,
            registeredTimestamp = time,
            sampleCount = samples,
            signaturesJson = sigs,
            matName = name
        )
    }

    fun savePrayerMatProfile(profile: com.example.data.model.PrayerMatProfile) {
        prefs.edit()
            .putBoolean("prayer_mat_is_registered", profile.isRegistered)
            .putLong("prayer_mat_registered_time", profile.registeredTimestamp)
            .putInt("prayer_mat_sample_count", profile.sampleCount)
            .putString("prayer_mat_signatures_json", profile.signaturesJson)
            .putString("prayer_mat_name", profile.matName)
            .apply()
        notifyChanged()
    }

    fun getFajrAlarmConfig(): FajrAlarmConfig {
        val isEnabled = prefs.getBoolean("fajr_alarm_enabled", true)
        val preAlarm = prefs.getInt("fajr_pre_alarm_min", 0)
        val challengeStr = prefs.getString("fajr_challenge_type", FajrChallengeType.PRAYER_MAT_RECOGNITION.name)
        val challengeType = try { FajrChallengeType.valueOf(challengeStr ?: "") } catch (e: Exception) { FajrChallengeType.PRAYER_MAT_RECOGNITION }
        val fallbackStr = prefs.getString("fajr_fallback_challenge", FajrChallengeType.MATH.name)
        val fallbackType = try { FajrChallengeType.valueOf(fallbackStr ?: "") } catch (e: Exception) { FajrChallengeType.MATH }
        val diffStr = prefs.getString("fajr_challenge_diff", ChallengeDifficulty.MEDIUM.name)
        val diff = try { ChallengeDifficulty.valueOf(diffStr ?: "") } catch (e: Exception) { ChallengeDifficulty.MEDIUM }
        val vib = prefs.getBoolean("fajr_vibration", true)
        val vol = prefs.getFloat("fajr_vol", 1.0f)
        val reqFrames = prefs.getInt("fajr_req_frames", 6)
        val confThreshold = prefs.getFloat("fajr_conf_threshold", 0.65f)
        val snoozeMins = prefs.getInt("fajr_snooze_min", 5)
        val maxSnoozeCount = prefs.getInt("fajr_max_snoozes", 2)
        val wuduMins = prefs.getInt("fajr_wudu_timer_min", 5)
        val wuduGuideEnabled = prefs.getBoolean("fajr_wudu_guide_enabled", true)
        val wuduGuideDetailed = prefs.getBoolean("fajr_wudu_guide_detailed", false)
        val profile = getPrayerMatProfile()

        return FajrAlarmConfig(
            isEnabled = isEnabled,
            preAlarmMinutes = preAlarm,
            challengeType = challengeType,
            fallbackChallengeType = fallbackType,
            difficulty = diff,
            isVibrationEnabled = vib,
            volume = vol,
            requiredConsecutiveFrames = reqFrames,
            confidenceThreshold = confThreshold,
            snoozeDurationMinutes = snoozeMins,
            maxSnoozes = maxSnoozeCount,
            wuduTimerMinutes = wuduMins,
            isWuduGuideEnabled = wuduGuideEnabled,
            isWuduGuideDetailed = wuduGuideDetailed,
            prayerMatProfile = profile
        )
    }

    fun saveFajrAlarmConfig(config: FajrAlarmConfig) {
        prefs.edit()
            .putBoolean("fajr_alarm_enabled", config.isEnabled)
            .putInt("fajr_pre_alarm_min", config.preAlarmMinutes)
            .putString("fajr_challenge_type", config.challengeType.name)
            .putString("fajr_fallback_challenge", config.fallbackChallengeType.name)
            .putString("fajr_challenge_diff", config.difficulty.name)
            .putBoolean("fajr_vibration", config.isVibrationEnabled)
            .putFloat("fajr_vol", config.volume)
            .putInt("fajr_req_frames", config.requiredConsecutiveFrames)
            .putFloat("fajr_conf_threshold", config.confidenceThreshold)
            .putInt("fajr_snooze_min", config.snoozeDurationMinutes)
            .putInt("fajr_max_snoozes", config.maxSnoozes)
            .putInt("fajr_wudu_timer_min", config.wuduTimerMinutes)
            .putBoolean("fajr_wudu_guide_enabled", config.isWuduGuideEnabled)
            .putBoolean("fajr_wudu_guide_detailed", config.isWuduGuideDetailed)
            .apply()
        savePrayerMatProfile(config.prayerMatProfile)
        notifyChanged()
    }

    // Active Snooze Runtime State
    var currentSnoozeCount: Int
        get() = prefs.getInt("active_snooze_count", 0)
        set(value) {
            prefs.edit().putInt("active_snooze_count", value).apply()
            notifyChanged()
        }

    var snoozeTargetMillis: Long
        get() = prefs.getLong("active_snooze_target_millis", 0L)
        set(value) {
            prefs.edit().putLong("active_snooze_target_millis", value).apply()
            notifyChanged()
        }

    fun resetSnoozeState() {
        prefs.edit()
            .putInt("active_snooze_count", 0)
            .putLong("active_snooze_target_millis", 0L)
            .apply()
        notifyChanged()
    }


    // App Preferences
    var isAmoledDarkTheme: Boolean
        get() = prefs.getBoolean("amoled_dark", false)
        set(value) {
            prefs.edit().putBoolean("amoled_dark", value).apply()
            notifyChanged()
        }

    var quranFontSize: Float
        get() = prefs.getFloat("quran_font_size", 22f)
        set(value) {
            prefs.edit().putFloat("quran_font_size", value).apply()
            notifyChanged()
        }

    var quranShowTranslation: Boolean
        get() = prefs.getBoolean("quran_show_trans", true)
        set(value) {
            prefs.edit().putBoolean("quran_show_trans", value).apply()
            notifyChanged()
        }

    var quranShowTransliteration: Boolean
        get() = prefs.getBoolean("quran_show_translit", true)
        set(value) {
            prefs.edit().putBoolean("quran_show_translit", value).apply()
            notifyChanged()
        }

    var lastReadSurah: Int
        get() = prefs.getInt("last_read_surah", 1)
        set(value) {
            prefs.edit().putInt("last_read_surah", value).apply()
            notifyChanged()
        }

    var lastReadAyah: Int
        get() = prefs.getInt("last_read_ayah", 1)
        set(value) {
            prefs.edit().putInt("last_read_ayah", value).apply()
            notifyChanged()
        }
}
