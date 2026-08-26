package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.DayCompletionStat
import com.example.data.model.DhikrItem
import com.example.data.model.FajrAlarmConfig
import com.example.data.model.HijriDate
import com.example.data.model.Madhhab
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerName
import com.example.data.model.PrayerRecordEntity
import com.example.data.model.PrayerStatus
import com.example.data.model.PrayerStreakStats
import com.example.data.model.QuranBookmarkEntity
import com.example.data.model.TasbihSessionEntity
import com.example.service.FajrAlarmScheduler
import com.example.service.HijriCalendarEngine
import com.example.service.PrayerCalculationEngine
import com.example.service.PrayerNotificationScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settings = SettingsManager(application)

    private val _todaySchedule = MutableStateFlow<DayPrayerSchedule?>(null)
    val todaySchedule: StateFlow<DayPrayerSchedule?> = _todaySchedule.asStateFlow()

    private val _tomorrowSchedule = MutableStateFlow<DayPrayerSchedule?>(null)
    val tomorrowSchedule: StateFlow<DayPrayerSchedule?> = _tomorrowSchedule.asStateFlow()

    private val _nextPrayerInfo = MutableStateFlow<NextPrayerInfo?>(null)
    val nextPrayerInfo: StateFlow<NextPrayerInfo?> = _nextPrayerInfo.asStateFlow()

    private val _hijriDate = MutableStateFlow<HijriDate?>(null)
    val hijriDate: StateFlow<HijriDate?> = _hijriDate.asStateFlow()

    private val _qiblaBearing = MutableStateFlow(0f)
    val qiblaBearing: StateFlow<Float> = _qiblaBearing.asStateFlow()

    private val _fajrAlarmConfig = MutableStateFlow(settings.getFajrAlarmConfig())
    val fajrAlarmConfig: StateFlow<FajrAlarmConfig> = _fajrAlarmConfig.asStateFlow()

    private val _todayRecords = MutableStateFlow<Map<PrayerName, PrayerStatus>>(emptyMap())
    val todayRecords: StateFlow<Map<PrayerName, PrayerStatus>> = _todayRecords.asStateFlow()

    private val _selectedDateKey = MutableStateFlow(getTodayDateKey())
    val selectedDateKey: StateFlow<String> = _selectedDateKey.asStateFlow()

    private val _selectedDateDisplay = MutableStateFlow("Today")
    val selectedDateDisplay: StateFlow<String> = _selectedDateDisplay.asStateFlow()

    private val _selectedDateRecords = MutableStateFlow<Map<PrayerName, PrayerStatus>>(emptyMap())
    val selectedDateRecords: StateFlow<Map<PrayerName, PrayerStatus>> = _selectedDateRecords.asStateFlow()

    private val _selectedDateSchedule = MutableStateFlow<DayPrayerSchedule?>(null)
    val selectedDateSchedule: StateFlow<DayPrayerSchedule?> = _selectedDateSchedule.asStateFlow()

    private val _streakStats = MutableStateFlow(PrayerStreakStats())
    val streakStats: StateFlow<PrayerStreakStats> = _streakStats.asStateFlow()

    val customDhikrs: StateFlow<List<DhikrItem>> = db.dhikrDao().getAllCustomDhikrs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val bookmarks: StateFlow<List<QuranBookmarkEntity>> = db.quranBookmarkDao().getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active Tasbih State
    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount: StateFlow<Int> = _tasbihCount.asStateFlow()

    private val _tasbihTarget = MutableStateFlow(33)
    val tasbihTarget: StateFlow<Int> = _tasbihTarget.asStateFlow()

    private val _tasbihPhrase = MutableStateFlow("سُبْحَانَ اللَّهِ")
    val tasbihPhrase: StateFlow<String> = _tasbihPhrase.asStateFlow()

    init {
        refreshAllData()
        startCountdownTicker()
        listenToSettingsUpdates()
        observePrayerRecords()
        updateSelectedDateSchedule(_selectedDateKey.value)
    }

    private fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun refreshAllData() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val schedToday = PrayerCalculationEngine.calculateTimes(
            year = year,
            month = month,
            day = day,
            latitude = settings.latitude,
            longitude = settings.longitude,
            timezone = settings.timezoneOffsetHours,
            method = settings.calculationMethod,
            madhhab = settings.madhhab,
            highLatRule = settings.highLatitudeRule,
            manualOffsetsMinutes = settings.getAllPrayerOffsets(),
            locationName = settings.cityName
        )
        _todaySchedule.value = schedToday

        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val schedTomorrow = PrayerCalculationEngine.calculateTimes(
            year = tomorrowCal.get(Calendar.YEAR),
            month = tomorrowCal.get(Calendar.MONTH) + 1,
            day = tomorrowCal.get(Calendar.DAY_OF_MONTH),
            latitude = settings.latitude,
            longitude = settings.longitude,
            timezone = settings.timezoneOffsetHours,
            method = settings.calculationMethod,
            madhhab = settings.madhhab,
            highLatRule = settings.highLatitudeRule,
            manualOffsetsMinutes = settings.getAllPrayerOffsets(),
            locationName = settings.cityName
        )
        _tomorrowSchedule.value = schedTomorrow

        _nextPrayerInfo.value = PrayerCalculationEngine.getNextPrayer(schedToday, schedTomorrow)
        _hijriDate.value = HijriCalendarEngine.getHijriDate(cal, settings.hijriDayAdjustment)
        _qiblaBearing.value = PrayerCalculationEngine.calculateQiblaBearing(settings.latitude, settings.longitude).toFloat()
        _fajrAlarmConfig.value = settings.getFajrAlarmConfig()

        updateSelectedDateSchedule(_selectedDateKey.value)
        FajrAlarmScheduler.scheduleFajrAlarm(getApplication())
        PrayerNotificationScheduler.scheduleAllPrayerNotifications(getApplication())
    }

    private fun updateSelectedDateSchedule(dateKey: String) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateKey) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)

            _selectedDateSchedule.value = PrayerCalculationEngine.calculateTimes(
                year = year,
                month = month,
                day = day,
                latitude = settings.latitude,
                longitude = settings.longitude,
                timezone = settings.timezoneOffsetHours,
                method = settings.calculationMethod,
                madhhab = settings.madhhab,
                highLatRule = settings.highLatitudeRule,
                manualOffsetsMinutes = settings.getAllPrayerOffsets(),
                locationName = settings.cityName
            )
        } catch (e: Exception) {
            _selectedDateSchedule.value = _todaySchedule.value
        }
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                val tSched = _todaySchedule.value
                val tmSched = _tomorrowSchedule.value
                if (tSched != null && tmSched != null) {
                    _nextPrayerInfo.value = PrayerCalculationEngine.getNextPrayer(tSched, tmSched)
                }
                delay(1000L)
            }
        }
    }

    private fun listenToSettingsUpdates() {
        viewModelScope.launch {
            settings.settingsChangedFlow.collect {
                refreshAllData()
            }
        }
    }

    private fun observePrayerRecords() {
        viewModelScope.launch {
            db.prayerRecordDao().getAllRecords().collect { allRecords ->
                if (allRecords.isEmpty()) {
                    seedInitialPrayerHistory()
                    return@collect
                }
                processAllRecords(allRecords)
            }
        }
    }

    private suspend fun seedInitialPrayerHistory() {
        // Seed the last 3 days of completed prayers so users have an authentic starting streak
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val initialRecords = mutableListOf<PrayerRecordEntity>()
        val fardPrayers = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)

        for (daysAgo in 3 downTo 1) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val dKey = sdf.format(cal.time)
            fardPrayers.forEach { prayer ->
                initialRecords.add(
                    PrayerRecordEntity(
                        dateKey = dKey,
                        prayerName = prayer.name,
                        status = PrayerStatus.ON_TIME.name,
                        timestamp = cal.timeInMillis
                    )
                )
            }
        }
        db.prayerRecordDao().insertAll(initialRecords)
    }

    private fun processAllRecords(allRecords: List<PrayerRecordEntity>) {
        val todayKey = getTodayDateKey()
        val recordsByDate = allRecords.groupBy { it.dateKey }

        // 1. Update today's records
        val todayList = recordsByDate[todayKey] ?: emptyList()
        val todayMap = mutableMapOf<PrayerName, PrayerStatus>()
        todayList.forEach { entity ->
            try {
                todayMap[PrayerName.valueOf(entity.prayerName)] = PrayerStatus.valueOf(entity.status)
            } catch (_: Exception) {}
        }
        _todayRecords.value = todayMap

        // 2. Update selected date's records
        val selectedKey = _selectedDateKey.value
        val selectedList = recordsByDate[selectedKey] ?: emptyList()
        val selectedMap = mutableMapOf<PrayerName, PrayerStatus>()
        selectedList.forEach { entity ->
            try {
                selectedMap[PrayerName.valueOf(entity.prayerName)] = PrayerStatus.valueOf(entity.status)
            } catch (_: Exception) {}
        }
        _selectedDateRecords.value = selectedMap

        // 3. Compute Weekly History (last 7 days)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayNameSdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumSdf = SimpleDateFormat("d", Locale.getDefault())
        val weeklyList = mutableListOf<DayCompletionStat>()

        val fardList = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dKey = sdf.format(cal.time)
            val dayEntities = recordsByDate[dKey] ?: emptyList()
            val completedCount = dayEntities.count { entity ->
                try {
                    val pName = PrayerName.valueOf(entity.prayerName)
                    val pStatus = PrayerStatus.valueOf(entity.status)
                    pName in fardList && pStatus.isCompleted
                } catch (_: Exception) {
                    false
                }
            }
            weeklyList.add(
                DayCompletionStat(
                    dateKey = dKey,
                    dayName = dayNameSdf.format(cal.time),
                    dayNumber = dayNumSdf.format(cal.time),
                    completedCount = completedCount,
                    isToday = (dKey == todayKey),
                    isAllCompleted = (completedCount >= 5)
                )
            )
        }

        // 4. Compute Streaks
        // A day is considered completed if at least all 5 Fard prayers were completed
        var currentStreak = 0
        val todayCompletedCount = todayMap.filter { it.key in fardList && it.value.isCompleted }.size

        // Check if today is completed (5/5)
        var checkCal = Calendar.getInstance()
        if (todayCompletedCount >= 5) {
            currentStreak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // If today is not yet 5/5, streak is based on unbroken days ending yesterday
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dKey = sdf.format(checkCal.time)
            val dayEntities = recordsByDate[dKey] ?: emptyList()
            val completed = dayEntities.count { entity ->
                try {
                    val pName = PrayerName.valueOf(entity.prayerName)
                    val pStatus = PrayerStatus.valueOf(entity.status)
                    pName in fardList && pStatus.isCompleted
                } catch (_: Exception) {
                    false
                }
            }
            if (completed >= 5) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        // Longest Streak calculation
        val sortedDateKeys = recordsByDate.keys.sorted()
        var longestStreak = currentStreak
        var tempStreak = 0
        var prevCal: Calendar? = null

        sortedDateKeys.forEach { dKey ->
            val dayEntities = recordsByDate[dKey] ?: emptyList()
            val completed = dayEntities.count { entity ->
                try {
                    val pName = PrayerName.valueOf(entity.prayerName)
                    val pStatus = PrayerStatus.valueOf(entity.status)
                    pName in fardList && pStatus.isCompleted
                } catch (_: Exception) {
                    false
                }
            }
            if (completed >= 5) {
                try {
                    val curCal = Calendar.getInstance().apply { time = sdf.parse(dKey) ?: Date() }
                    if (prevCal != null) {
                        val diffDays = ((curCal.timeInMillis - prevCal!!.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                        if (diffDays == 1) {
                            tempStreak++
                        } else {
                            tempStreak = 1
                        }
                    } else {
                        tempStreak = 1
                    }
                    prevCal = curCal
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak
                    }
                } catch (_: Exception) {}
            } else {
                tempStreak = 0
                prevCal = null
            }
        }

        // Stats summary
        var onTimeCount = 0
        var totalRecorded = 0
        var qadaCount = 0

        allRecords.forEach { entity ->
            try {
                val pStatus = PrayerStatus.valueOf(entity.status)
                totalRecorded++
                if (pStatus == PrayerStatus.ON_TIME || pStatus == PrayerStatus.IN_CONGREGATION) {
                    onTimeCount++
                } else if (pStatus == PrayerStatus.QADA || pStatus == PrayerStatus.MISSED) {
                    qadaCount++
                }
            } catch (_: Exception) {}
        }

        val onTimePercent = if (totalRecorded > 0) (onTimeCount.toFloat() / totalRecorded * 100f) else 100f

        _streakStats.value = PrayerStreakStats(
            currentStreakDays = currentStreak,
            longestStreakDays = maxOf(longestStreak, currentStreak),
            totalPrayersRecorded = totalRecorded,
            onTimePercentage = onTimePercent,
            qadaCountPending = qadaCount,
            weeklyHistory = weeklyList,
            todayCompletedCount = todayCompletedCount,
            todayTotalCount = 5
        )
    }

    // Date Navigation for Prayer Tracking
    fun selectDate(dateKey: String) {
        _selectedDateKey.value = dateKey
        val todayKey = getTodayDateKey()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val date = sdf.parse(dateKey) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            val nowCal = Calendar.getInstance()

            val diff = ((cal.timeInMillis - nowCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            _selectedDateDisplay.value = when (diff) {
                0 -> "Today"
                -1 -> "Yesterday"
                1 -> "Tomorrow"
                else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(date)
            }
        } catch (_: Exception) {
            _selectedDateDisplay.value = dateKey
        }
        updateSelectedDateSchedule(dateKey)
        viewModelScope.launch {
            db.prayerRecordDao().getAllRecords().collect {
                processAllRecords(it)
            }
        }
    }

    fun selectPreviousDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val date = sdf.parse(_selectedDateKey.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, -1)
            }
            selectDate(sdf.format(cal.time))
        } catch (_: Exception) {}
    }

    fun selectNextDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val date = sdf.parse(_selectedDateKey.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, 1)
            }
            selectDate(sdf.format(cal.time))
        } catch (_: Exception) {}
    }

    fun selectToday() {
        selectDate(getTodayDateKey())
    }

    fun togglePrayerStatus(prayer: PrayerName, dateKey: String = _selectedDateKey.value) {
        val current = _selectedDateRecords.value[prayer]
        val nextStatus = when (current) {
            null -> PrayerStatus.ON_TIME
            PrayerStatus.ON_TIME -> PrayerStatus.IN_CONGREGATION
            PrayerStatus.IN_CONGREGATION -> PrayerStatus.LATE
            PrayerStatus.LATE -> PrayerStatus.QADA
            PrayerStatus.QADA -> PrayerStatus.MISSED
            PrayerStatus.MISSED -> null
        }

        viewModelScope.launch {
            if (nextStatus == null) {
                db.prayerRecordDao().deleteRecord(dateKey, prayer.name)
            } else {
                val entity = PrayerRecordEntity(
                    dateKey = dateKey,
                    prayerName = prayer.name,
                    status = nextStatus.name,
                    timestamp = System.currentTimeMillis()
                )
                db.prayerRecordDao().insertOrUpdate(entity)
            }
        }
    }

    fun setPrayerStatus(prayer: PrayerName, status: PrayerStatus, dateKey: String = _selectedDateKey.value) {
        viewModelScope.launch {
            val entity = PrayerRecordEntity(
                dateKey = dateKey,
                prayerName = prayer.name,
                status = status.name,
                timestamp = System.currentTimeMillis()
            )
            db.prayerRecordDao().insertOrUpdate(entity)
        }
    }

    fun deletePrayerRecord(prayer: PrayerName, dateKey: String = _selectedDateKey.value) {
        viewModelScope.launch {
            db.prayerRecordDao().deleteRecord(dateKey, prayer.name)
        }
    }

    fun markAllFardPrayed(status: PrayerStatus = PrayerStatus.ON_TIME, dateKey: String = _selectedDateKey.value) {
        viewModelScope.launch {
            val fardList = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
            val list = fardList.map { prayer ->
                PrayerRecordEntity(
                    dateKey = dateKey,
                    prayerName = prayer.name,
                    status = status.name,
                    timestamp = System.currentTimeMillis()
                )
            }
            db.prayerRecordDao().insertAll(list)
        }
    }

    fun clearDayRecords(dateKey: String = _selectedDateKey.value) {
        viewModelScope.launch {
            db.prayerRecordDao().deleteRecordsForDate(dateKey)
        }
    }

    fun recordPrayerStatus(prayer: PrayerName, status: PrayerStatus) {
        setPrayerStatus(prayer, status, getTodayDateKey())
    }

    fun setLocation(city: CityLocation) {
        settings.cityName = city.cityName
        settings.countryName = city.country
        settings.latitude = city.latitude
        settings.longitude = city.longitude
        // Set standard timezone offset
        settings.timezoneOffsetHours = when (city.cityName) {
            "Makkah", "Madinah", "Doha" -> 3.0
            "Cairo" -> 2.0
            "Istanbul" -> 3.0
            "London" -> 0.0
            "New York", "Toronto" -> -5.0
            "Dubai" -> 4.0
            "Karachi" -> 5.0
            "Jakarta" -> 7.0
            "Kuala Lumpur", "Singapore" -> 8.0
            "Paris", "Berlin", "Algiers", "Rabat" -> 1.0
            "Sydney" -> 10.0
            "Chicago" -> -6.0
            "Los Angeles" -> -8.0
            else -> 3.0
        }
        refreshAllData()
    }

    fun detectAndSetDeviceLocation(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = com.example.service.NativeLocationHelper.getBestNativeLocation(getApplication())
            if (result != null) {
                settings.latitude = result.latitude
                settings.longitude = result.longitude
                settings.cityName = result.cityName
                if (result.countryName.isNotBlank()) {
                    settings.countryName = result.countryName
                }
                settings.timezoneOffsetHours = result.timezoneOffsetHours
                refreshAllData()
                onResult(true, "Updated location to ${result.cityName}")
            } else {
                onResult(false, "Could not obtain GPS/Network location. Please select city manually.")
            }
        }
    }

    fun updateCalculationMethod(method: CalculationMethod) {
        settings.calculationMethod = method
    }

    fun updateMadhhab(madhhab: Madhhab) {
        settings.madhhab = madhhab
    }

    fun updatePrayerOffset(prayer: PrayerName, delta: Int) {
        val current = settings.getPrayerOffset(prayer)
        val newOffset = (current + delta).coerceIn(-60, 60)
        settings.setPrayerOffset(prayer, newOffset)
    }

    fun updateFajrAlarmConfig(config: FajrAlarmConfig) {
        settings.saveFajrAlarmConfig(config)
        _fajrAlarmConfig.value = config
        FajrAlarmScheduler.scheduleFajrAlarm(getApplication())
    }

    // Tasbih interactions
    fun incrementTasbih() {
        val newCount = _tasbihCount.value + 1
        _tasbihCount.value = newCount
        if (newCount == _tasbihTarget.value) {
            // Target reached
            viewModelScope.launch {
                db.tasbihSessionDao().insertSession(
                    TasbihSessionEntity(
                        phraseArabic = _tasbihPhrase.value,
                        phraseEnglish = "Tasbih Session",
                        totalCount = newCount
                    )
                )
            }
        }
    }

    fun resetTasbih() {
        _tasbihCount.value = 0
    }

    fun setTasbihTarget(target: Int) {
        _tasbihTarget.value = target
        _tasbihCount.value = 0
    }

    fun setTasbihPhrase(phrase: String) {
        _tasbihPhrase.value = phrase
        _tasbihCount.value = 0
    }

    // Quran bookmarking
    fun toggleBookmark(surahNumber: Int, surahName: String, ayahNumber: Int, snippet: String) {
        viewModelScope.launch {
            val exists = db.quranBookmarkDao().isBookmarked(surahNumber, ayahNumber)
            if (exists) {
                db.quranBookmarkDao().deleteBookmark(surahNumber, ayahNumber)
            } else {
                db.quranBookmarkDao().insertBookmark(
                    QuranBookmarkEntity(
                        surahNumber = surahNumber,
                        surahName = surahName,
                        ayahNumber = ayahNumber,
                        snippet = snippet
                    )
                )
            }
        }
    }

    fun snoozeFajrAlarm(): Boolean {
        val config = _fajrAlarmConfig.value
        if (config.snoozeDurationMinutes <= 0 || config.maxSnoozes <= 0) return false
        val currentCount = settings.currentSnoozeCount
        if (currentCount >= config.maxSnoozes) return false

        settings.currentSnoozeCount = currentCount + 1
        FajrAlarmScheduler.scheduleSnoozeAlarm(getApplication(), config.snoozeDurationMinutes)
        return true
    }

    fun cancelSnooze() {
        FajrAlarmScheduler.cancelSnoozeAlarm(getApplication())
    }

    fun togglePrayerNotification(prayer: PrayerName, enabled: Boolean) {
        settings.setPrayerNotificationEnabled(prayer, enabled)
        PrayerNotificationScheduler.scheduleAllPrayerNotifications(getApplication())
    }

    fun triggerTestPrayerNotification(prayer: PrayerName = PrayerName.DHUHR) {
        PrayerNotificationScheduler.triggerTestNotification(getApplication(), prayer)
    }

    fun getSettingsManager() = settings
}

