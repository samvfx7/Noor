package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus
import com.example.service.PrayerCalculationEngine
import com.example.ui.theme.MinimalActiveGreen
import com.example.ui.theme.MinimalPrimaryLight
import com.example.ui.theme.MinimalTertiaryContainerLight
import com.example.ui.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PrayerViewModel,
    onNavigateToPrayerTimes: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToFajrAlarm: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToAdhkar: () -> Unit,
    onNavigateToTasbih: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTriggerAlarmTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todaySchedule by viewModel.todaySchedule.collectAsState()
    val nextInfo by viewModel.nextPrayerInfo.collectAsState()
    val hijriDate by viewModel.hijriDate.collectAsState()
    val todayRecords by viewModel.todayRecords.collectAsState()
    val streakStats by viewModel.streakStats.collectAsState()
    val settings = viewModel.getSettingsManager()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Header with Minimalist Typography & Location Pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ASSALAMU ALAIKUM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = hijriDate?.formatted ?: "1447 AH",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Clean Minimalist Location Pill with Pulsing Dot
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .clickable { onNavigateToSettings() }
                        .testTag("location_pill_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MinimalActiveGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = settings.cityName.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Next Prayer Countdown Hero Card (Clean Periwinkle Minimalist Banner)
        item {
            nextInfo?.let { next ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val prayerTimeFormatted = timeFormat.format(Date(next.nextPrayerTimeMillis))
                val hours = next.remainingMillis / (1000 * 60 * 60)
                val mins = (next.remainingMillis / (1000 * 60)) % 60
                val countdownStr = if (hours > 0) "Starts in ${hours}h ${mins}m" else "Starts in ${mins}m"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("hero_countdown_card"),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Ambient decorative background shape
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
                        )
                        Icon(
                            imageVector = Icons.Default.Nightlight,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(20.dp)
                                .size(36.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "UPCOMING: ${next.nextPrayer.displayName.uppercase(Locale.getDefault())}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = next.nextPrayer.arabicName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = prayerTimeFormatted,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = (-1.5).sp
                            )

                            Text(
                                text = countdownStr,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToFajrAlarm,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fajr Alarm",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    onClick = {
                                        viewModel.triggerTestPrayerNotification(next.nextPrayer)
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Test Alert",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Test Alert",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Access 2x2 Grid with Clean Minimalist Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalFeatureCard(
                        title = "Quran",
                        subtitle = "114 Surahs",
                        icon = Icons.Default.AutoStories,
                        badgeColor = MinimalTertiaryContainerLight,
                        badgeIconTint = MaterialTheme.colorScheme.tertiary,
                        onClick = onNavigateToQuran,
                        modifier = Modifier.weight(1f)
                    )
                    MinimalFeatureCard(
                        title = "Qibla",
                        subtitle = "Compass",
                        icon = Icons.Default.CompassCalibration,
                        badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                        badgeIconTint = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToQibla,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalFeatureCard(
                        title = "Adhkar",
                        subtitle = "Daily Remembrances",
                        icon = Icons.Default.SelfImprovement,
                        badgeColor = MaterialTheme.colorScheme.surfaceVariant,
                        badgeIconTint = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToAdhkar,
                        modifier = Modifier.weight(1f)
                    )
                    MinimalFeatureCard(
                        title = "Tasbih",
                        subtitle = "Digital Counter",
                        icon = Icons.Default.TouchApp,
                        badgeColor = MaterialTheme.colorScheme.primaryContainer,
                        badgeIconTint = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToTasbih,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Daily Prayer Times Timeline & Tracker Check-in
        item {
            todaySchedule?.let { sched ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val obligatoryPrayers = listOf(
                    PrayerName.FAJR to sched.fajrMillis,
                    PrayerName.SUNRISE to sched.sunriseMillis,
                    PrayerName.DHUHR to sched.dhuhrMillis,
                    PrayerName.ASR to sched.asrMillis,
                    PrayerName.MAGHRIB to sched.maghribMillis,
                    PrayerName.ISHA to sched.ishaMillis
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prayer Times",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { onNavigateToTracker() }
                            ) {
                                Text(
                                    text = "TRACKER ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        obligatoryPrayers.forEachIndexed { index, (prayer, timeMillis) ->
                            val isSunrise = prayer == PrayerName.SUNRISE
                            val currentStatus = todayRecords[prayer]
                            val isNext = nextInfo?.nextPrayer == prayer
                            val timeStr = timeFormat.format(Date(timeMillis))
                            val isNotifEnabled = if (isSunrise) false else settings.isPrayerNotificationEnabled(prayer)

                            MinimalPrayerRow(
                                prayer = prayer,
                                timeFormatted = timeStr,
                                isSunrise = isSunrise,
                                isNext = isNext,
                                isNotifEnabled = isNotifEnabled,
                                onToggleNotif = { enabled ->
                                    viewModel.togglePrayerNotification(prayer, enabled)
                                },
                                status = currentStatus,
                                onStatusSelected = { status ->
                                    viewModel.recordPrayerStatus(prayer, status)
                                }
                            )

                            if (index < obligatoryPrayers.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        // 5. Daily Streak & Prayer Progress Hero Card
        item {
            val todayFardCompleted = todayRecords.filter {
                it.key in listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA) &&
                        it.value.isCompleted
            }.size
            val todayProgress = (todayFardCompleted.toFloat() / 5f).coerceIn(0f, 1f)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToTracker() }
                    .testTag("home_prayer_tracker_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = "Streak",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Daily Prayer Tracker",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${streakStats.currentStreakDays} Day Streak 🔥 • ${streakStats.onTimePercentage.toInt()}% On-Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { onNavigateToTracker() }
                        ) {
                            Text(
                                text = "$todayFardCompleted / 5",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { todayProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("home_prayer_progress_bar"),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    badgeIconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MinimalPrayerRow(
    prayer: PrayerName,
    timeFormatted: String,
    isSunrise: Boolean,
    isNext: Boolean,
    isNotifEnabled: Boolean = true,
    onToggleNotif: (Boolean) -> Unit = {},
    status: PrayerStatus?,
    onStatusSelected: (PrayerStatus) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val rowBg = when {
        isNext -> MaterialTheme.colorScheme.surfaceVariant
        status == PrayerStatus.ON_TIME -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    val rowBorder = if (isNext) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = rowBg),
        border = rowBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSunrise) Icons.Default.WbSunny
                    else if (prayer == PrayerName.MAGHRIB || prayer == PrayerName.ISHA) Icons.Default.Nightlight
                    else Icons.Default.WbSunny,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = prayer.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = prayer.arabicName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (!isSunrise) {
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onToggleNotif(!isNotifEnabled) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("notif_toggle_${prayer.name}")
                    ) {
                        Icon(
                            imageVector = if (isNotifEnabled) Icons.Default.Notifications else Icons.Default.Notifications,
                            contentDescription = "Notification alert toggle",
                            tint = if (isNotifEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box {
                        Surface(
                            onClick = { showMenu = true },
                            shape = CircleShape,
                            color = if (status == PrayerStatus.ON_TIME) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("prayer_status_${prayer.name}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (status == PrayerStatus.ON_TIME) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "On Time",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Status",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✓ Prayed On Time") },
                                onClick = {
                                    onStatusSelected(PrayerStatus.ON_TIME)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🕌 In Congregation") },
                                onClick = {
                                    onStatusSelected(PrayerStatus.IN_CONGREGATION)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⏳ Prayed Late") },
                                onClick = {
                                    onStatusSelected(PrayerStatus.LATE)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔄 Made Up (Qada)") },
                                onClick = {
                                    onStatusSelected(PrayerStatus.QADA)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("✕ Missed") },
                                onClick = {
                                    onStatusSelected(PrayerStatus.MISSED)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
