package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.SettingsManager
import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerNotificationScheduler {
    const val CHANNEL_ID_PRAYERS = "prayer_times_notification_channel_v2"
    const val CHANNEL_NAME_PRAYERS = "Prayer Times & Adhan Alerts"
    
    const val ACTION_PRAYER_TIME_TRIGGER = "com.example.ACTION_PRAYER_TIME_TRIGGER"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_DATE_KEY = "extra_date_key"
    const val EXTRA_TIME_STR = "extra_time_str"
    const val EXTRA_LOCATION_STR = "extra_location_str"

    private const val BASE_ALARM_REQUEST_CODE = 2000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID_PRAYERS,
                CHANNEL_NAME_PRAYERS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when prayer time enters with direct quick-logging buttons"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Calculates upcoming prayer times based on the user's current coordinates/method
     * and sets precise alarms for each prayer time.
     */
    fun scheduleAllPrayerNotifications(context: Context) {
        createNotificationChannel(context)

        val settings = SettingsManager(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val prayers = listOf(
            PrayerName.FAJR,
            PrayerName.DHUHR,
            PrayerName.ASR,
            PrayerName.MAGHRIB,
            PrayerName.ISHA
        )

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

        prayers.forEachIndexed { index, prayer ->
            if (!settings.isPrayerNotificationEnabled(prayer)) {
                cancelPrayerNotification(context, prayer)
                return@forEachIndexed
            }

            val todayTimeMillis = when (prayer) {
                PrayerName.FAJR -> scheduleToday.fajrMillis
                PrayerName.DHUHR -> scheduleToday.dhuhrMillis
                PrayerName.ASR -> scheduleToday.asrMillis
                PrayerName.MAGHRIB -> scheduleToday.maghribMillis
                PrayerName.ISHA -> scheduleToday.ishaMillis
                else -> 0L
            }

            val tomorrowTimeMillis = when (prayer) {
                PrayerName.FAJR -> scheduleTomorrow.fajrMillis
                PrayerName.DHUHR -> scheduleTomorrow.dhuhrMillis
                PrayerName.ASR -> scheduleTomorrow.asrMillis
                PrayerName.MAGHRIB -> scheduleTomorrow.maghribMillis
                PrayerName.ISHA -> scheduleTomorrow.ishaMillis
                else -> 0L
            }

            val targetMillis: Long
            val dateKey: String

            if (todayTimeMillis > now) {
                targetMillis = todayTimeMillis
                dateKey = sdf.format(cal.time)
            } else {
                targetMillis = tomorrowTimeMillis
                dateKey = sdf.format(tomorrowCal.time)
            }

            val timeFormatted = timeSdf.format(Date(targetMillis))
            val requestCode = BASE_ALARM_REQUEST_CODE + index

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_TIME_TRIGGER
                putExtra(EXTRA_PRAYER_NAME, prayer.name)
                putExtra(EXTRA_DATE_KEY, dateKey)
                putExtra(EXTRA_TIME_STR, timeFormatted)
                putExtra(EXTRA_LOCATION_STR, settings.cityName)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val showIntent = Intent(context, MainActivity::class.java)
                    val showPending = PendingIntent.getActivity(
                        context,
                        requestCode + 100,
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
                Log.d("PrayerNotifScheduler", "Scheduled $prayer for $dateKey $timeFormatted ($targetMillis)")
            } catch (e: SecurityException) {
                Log.e("PrayerNotifScheduler", "Permission denied for exact alarm", e)
            }
        }
    }

    fun cancelPrayerNotification(context: Context, prayer: PrayerName) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val index = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA).indexOf(prayer)
        if (index < 0) return
        val requestCode = BASE_ALARM_REQUEST_CODE + index
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_TIME_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Builds and shows the interactive notification with Quick Logging action buttons.
     */
    fun showPrayerNotification(
        context: Context,
        prayerNameStr: String,
        dateKey: String,
        timeStr: String,
        locationStr: String
    ) {
        createNotificationChannel(context)

        val prayer = try { PrayerName.valueOf(prayerNameStr) } catch (e: Exception) { PrayerName.DHUHR }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = 3000 + prayer.ordinal

        // Tap notification -> Opens app into tracker
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_tracker", true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "✓ Prayed On Time"
        val logOnTimeIntent = Intent(context, PrayerLoggingReceiver::class.java).apply {
            action = PrayerLoggingReceiver.ACTION_QUICK_LOG_PRAYER
            putExtra(PrayerLoggingReceiver.EXTRA_PRAYER_NAME, prayer.name)
            putExtra(PrayerLoggingReceiver.EXTRA_DATE_KEY, dateKey)
            putExtra(PrayerLoggingReceiver.EXTRA_STATUS, PrayerStatus.ON_TIME.name)
            putExtra(PrayerLoggingReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val logOnTimePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 10,
            logOnTimeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "🕌 In Congregation"
        val logCongregationIntent = Intent(context, PrayerLoggingReceiver::class.java).apply {
            action = PrayerLoggingReceiver.ACTION_QUICK_LOG_PRAYER
            putExtra(PrayerLoggingReceiver.EXTRA_PRAYER_NAME, prayer.name)
            putExtra(PrayerLoggingReceiver.EXTRA_DATE_KEY, dateKey)
            putExtra(PrayerLoggingReceiver.EXTRA_STATUS, PrayerStatus.IN_CONGREGATION.name)
            putExtra(PrayerLoggingReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val logCongregationPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 20,
            logCongregationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 3: "⏰ Snooze 10m"
        val snoozeIntent = Intent(context, PrayerLoggingReceiver::class.java).apply {
            action = PrayerLoggingReceiver.ACTION_SNOOZE_NOTIFICATION
            putExtra(PrayerLoggingReceiver.EXTRA_PRAYER_NAME, prayer.name)
            putExtra(PrayerLoggingReceiver.EXTRA_DATE_KEY, dateKey)
            putExtra(PrayerLoggingReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 30,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🕌 Time for ${prayer.displayName} • حان وقت صلاة ${prayer.arabicName}"
        val body = "Prayer time has entered in $locationStr ($timeStr). Tap below to quickly log your prayer."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRAYERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$body\n\n“Indeed, prayer has been decreed upon the believers a decree of specified times.” [Quran 4:103]")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✓ Prayed On Time",
                logOnTimePendingIntent
            )
            .addAction(
                android.R.drawable.btn_star_big_on,
                "🕌 Congregation",
                logCongregationPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "⏰ Remind 10m",
                snoozePendingIntent
            )
            .build()

        notificationManager.notify(notifId, notification)
    }

    /**
     * Helper to trigger a sample notification immediately so users can test and preview
     * the notification UI and quick-logging directly.
     */
    fun triggerTestNotification(context: Context, prayer: PrayerName = PrayerName.ASR) {
        val settings = SettingsManager(context)
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        showPrayerNotification(
            context = context,
            prayerNameStr = prayer.name,
            dateKey = todayKey,
            timeStr = timeFormatted,
            locationStr = settings.cityName
        )
    }
}
