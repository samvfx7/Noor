package com.samvfx7.noor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.samvfx7.noor.data.local.SettingsManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("AlarmReceiver", "Received action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> {
                val settings = SettingsManager(context)
                val snoozeTarget = settings.snoozeTargetMillis
                val now = System.currentTimeMillis()
                if (snoozeTarget > now) {
                    val remainingMins = ((snoozeTarget - now) / 60000).toInt().coerceAtLeast(1)
                    FajrAlarmScheduler.scheduleSnoozeAlarm(context, remainingMins)
                } else {
                    settings.resetSnoozeState()
                    FajrAlarmScheduler.scheduleFajrAlarm(context)
                }
                // Schedule all 5 prayer time notifications based on location
                PrayerNotificationScheduler.scheduleAllPrayerNotifications(context)
            }
            PrayerNotificationScheduler.ACTION_PRAYER_TIME_TRIGGER -> {
                val prayerName = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
                val dateKey = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_DATE_KEY) ?: ""
                val timeStr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_TIME_STR) ?: ""
                val locationStr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_LOCATION_STR) ?: "Current Location"

                PrayerNotificationScheduler.showPrayerNotification(
                    context = context,
                    prayerNameStr = prayerName,
                    dateKey = dateKey,
                    timeStr = timeStr,
                    locationStr = locationStr
                )

                // Schedule next upcoming prayer notification
                PrayerNotificationScheduler.scheduleAllPrayerNotifications(context)
            }
            FajrAlarmScheduler.ACTION_FAJR_ALARM,
            FajrAlarmScheduler.ACTION_SNOOZE_ALARM -> {
                // Trigger AlarmRingingService
                val serviceIntent = Intent(context, AlarmRingingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // If this was regular Fajr alarm, schedule tomorrow's
                if (action == FajrAlarmScheduler.ACTION_FAJR_ALARM) {
                    FajrAlarmScheduler.scheduleFajrAlarm(context)
                }
            }
        }
    }
}

