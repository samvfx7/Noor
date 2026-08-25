package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.SettingsManager
import com.example.data.model.PrayerName
import java.util.Calendar

object FajrAlarmScheduler {
    const val ACTION_FAJR_ALARM = "com.example.ACTION_FAJR_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.example.ACTION_SNOOZE_ALARM"
    const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_CHALLENGE_TYPE = "extra_challenge_type"

    fun scheduleFajrAlarm(context: Context) {
        val settings = SettingsManager(context)
        val config = settings.getFajrAlarmConfig()
        if (!config.isEnabled) {
            cancelFajrAlarm(context)
            return
        }

        val cal = Calendar.getInstance()
        val scheduleToday = PrayerCalculationEngine.calculateTimes(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH),
            latitude = settings.latitude,
            longitude = settings.longitude,
            timezone = settings.timezoneOffsetHours,
            method = settings.calculationMethod,
            madhhab = settings.madhhab,
            highLatRule = settings.highLatitudeRule,
            manualOffsetsMinutes = settings.getAllPrayerOffsets(),
            locationName = settings.cityName
        )

        var alarmTargetMillis = scheduleToday.fajrMillis - (config.preAlarmMinutes * 60 * 1000L)
        val now = System.currentTimeMillis()

        // If today's Fajr alarm has already passed, schedule for tomorrow's Fajr
        if (alarmTargetMillis <= now) {
            val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val scheduleTomorrow = PrayerCalculationEngine.calculateTimes(
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
            alarmTargetMillis = scheduleTomorrow.fajrMillis - (config.preAlarmMinutes * 60 * 1000L)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FAJR_ALARM
            putExtra(EXTRA_PRAYER_NAME, PrayerName.FAJR.name)
            putExtra(EXTRA_CHALLENGE_TYPE, config.challengeType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val showIntent = Intent(context, com.example.MainActivity::class.java)
                val showPending = PendingIntent.getActivity(
                    context,
                    1002,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(alarmTargetMillis, showPending),
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTargetMillis, pendingIntent)
            }
            Log.d("FajrAlarmScheduler", "Scheduled Fajr alarm for: $alarmTargetMillis (in ${(alarmTargetMillis - now) / 60000} mins)")
        } catch (e: SecurityException) {
            Log.e("FajrAlarmScheduler", "Permission denied for exact alarm", e)
        }
    }

    fun scheduleSnoozeAlarm(context: Context, snoozeMinutes: Int) {
        val targetMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val settings = SettingsManager(context)
        settings.snoozeTargetMillis = targetMillis

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra(EXTRA_PRAYER_NAME, PrayerName.FAJR.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val showIntent = Intent(context, com.example.MainActivity::class.java)
                val showPending = PendingIntent.getActivity(
                    context,
                    1004,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(targetMillis, showPending),
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
            Log.d("FajrAlarmScheduler", "Scheduled Snooze alarm in $snoozeMinutes min ($targetMillis)")
        } catch (e: SecurityException) {
            Log.e("FajrAlarmScheduler", "Permission denied for snooze exact alarm", e)
        }
    }

    fun cancelSnoozeAlarm(context: Context) {
        val settings = SettingsManager(context)
        settings.resetSnoozeState()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelFajrAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FAJR_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
