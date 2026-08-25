package com.example.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.SettingsManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val settings = SettingsManager(context)
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

        val nextInfo = PrayerCalculationEngine.getNextPrayer(scheduleToday, scheduleTomorrow)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val prayerTimeStr = timeFormat.format(Date(nextInfo.nextPrayerTimeMillis))

        val hours = nextInfo.remainingMillis / (1000 * 60 * 60)
        val mins = (nextInfo.remainingMillis / (1000 * 60)) % 60
        val countdownStr = if (hours > 0) "in ${hours}h ${mins}m" else "in ${mins}m"

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.prayer_widget_layout)
            views.setTextViewText(R.id.widget_next_prayer, "${nextInfo.nextPrayer.displayName} $prayerTimeStr")
            views.setTextViewText(R.id.widget_countdown, countdownStr)
            views.setTextViewText(R.id.widget_location, "${settings.cityName} • ${settings.calculationMethod.title.take(15)}")

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
