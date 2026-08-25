package com.example.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.data.local.AppDatabase
import com.example.data.model.PrayerName
import com.example.data.model.PrayerRecordEntity
import com.example.data.model.PrayerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerLoggingReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_QUICK_LOG_PRAYER = "com.example.ACTION_QUICK_LOG_PRAYER"
        const val ACTION_SNOOZE_NOTIFICATION = "com.example.ACTION_SNOOZE_NOTIFICATION"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_DATE_KEY = "extra_date_key"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("PrayerLoggingReceiver", "Received action: $action")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 3000)

        when (action) {
            ACTION_QUICK_LOG_PRAYER -> {
                val prayerNameStr = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
                val statusStr = intent.getStringExtra(EXTRA_STATUS) ?: PrayerStatus.ON_TIME.name
                val dateKey = intent.getStringExtra(EXTRA_DATE_KEY)
                    ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                val prayer = try { PrayerName.valueOf(prayerNameStr) } catch (e: Exception) { PrayerName.DHUHR }
                val status = try { PrayerStatus.valueOf(statusStr) } catch (e: Exception) { PrayerStatus.ON_TIME }

                // Insert into Room Database in background
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val entity = PrayerRecordEntity(
                            dateKey = dateKey,
                            prayerName = prayer.name,
                            status = status.name,
                            timestamp = System.currentTimeMillis()
                        )
                        db.prayerRecordDao().insertOrUpdate(entity)
                        Log.d("PrayerLoggingReceiver", "Logged $prayer as $status in Room database")

                        // Show confirmation on UI thread
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "✓ ${prayer.displayName} logged as ${status.label}! Streak updated.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Replace notification with sleek success confirmation
                            val successNotif = NotificationCompat.Builder(
                                context,
                                PrayerNotificationScheduler.CHANNEL_ID_PRAYERS
                            )
                                .setSmallIcon(android.R.drawable.checkbox_on_background)
                                .setContentTitle("✓ ${prayer.displayName} Logged")
                                .setContentText("Recorded as ${status.label} • Daily progress updated")
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .setAutoCancel(true)
                                .setTimeoutAfter(4000)
                                .build()

                            notificationManager.notify(notifId, successNotif)
                        }

                        // Schedule the next cycle of prayer notifications
                        PrayerNotificationScheduler.scheduleAllPrayerNotifications(context)
                    } catch (e: Exception) {
                        Log.e("PrayerLoggingReceiver", "Error saving prayer record", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SNOOZE_NOTIFICATION -> {
                val prayerNameStr = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: PrayerName.DHUHR.name
                notificationManager.cancel(notifId)

                Toast.makeText(context, "Reminder snoozed for 10 minutes", Toast.LENGTH_SHORT).show()

                // Re-schedule reminder in 10 minutes
                Handler(Looper.getMainLooper()).postDelayed({
                    val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    PrayerNotificationScheduler.showPrayerNotification(
                        context = context,
                        prayerNameStr = prayerNameStr,
                        dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                        timeStr = timeFormatted,
                        locationStr = "Current Location"
                    )
                }, 10 * 60 * 1000L)
            }
        }
    }
}
