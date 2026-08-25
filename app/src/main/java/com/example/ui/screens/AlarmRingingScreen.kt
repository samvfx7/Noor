package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.FajrChallengeType
import com.example.data.model.MathProblem
import com.example.data.model.PrayerName
import com.example.data.model.PrayerStatus
import com.example.data.model.WuduStepItem
import com.example.service.AlarmRingingService
import com.example.ui.components.PrayerMatCameraScannerView
import com.example.ui.components.PrayerMatRegistrationDialog
import com.example.ui.viewmodel.PrayerViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

enum class FajrRoutineStage(val stepNumber: Int, val title: String, val subtitle: String) {
    WAKE(1, "Wake", "Dawn Awakening"),
    WUDU(2, "Wudu", "Ritual Purification"),
    PRAYER_MAT(3, "Prayer Mat", "Stand for Prayer"),
    PRAY(4, "Prayer", "Salah & Adhkar")
}

@Composable
fun AlarmRingingScreen(
    viewModel: PrayerViewModel,
    onDismissSuccess: () -> Unit
) {
    val context = LocalContext.current
    val settings = viewModel.getSettingsManager()
    val alarmConfig by viewModel.fajrAlarmConfig.collectAsState()
    val todaySchedule by viewModel.todaySchedule.collectAsState()

    var currentStage by remember { mutableStateOf(FajrRoutineStage.WAKE) }
    var isSnoozeActive by remember { mutableStateOf(false) }
    var snoozeRemainingSeconds by remember { mutableIntStateOf(alarmConfig.snoozeDurationMinutes * 60) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var isFallbackActive by remember { mutableStateOf(false) }
    var fallbackChallengeType by remember { mutableStateOf(alarmConfig.fallbackChallengeType) }
    var isFajrMarkedAsPrayed by remember { mutableStateOf(false) }

    // Live clock ticker
    var currentTimeFormatted by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeFormatted = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(now)
            delay(1000L)
        }
    }

    // Stop background alarm audio & vibration
    fun stopAlarmSound() {
        val stopIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_STOP_ALARM
        }
        context.startService(stopIntent)
    }

    // Handle Snooze
    fun handleSnooze() {
        stopAlarmSound()
        val didSnooze = viewModel.snoozeFajrAlarm()
        if (didSnooze) {
            isSnoozeActive = true
            snoozeRemainingSeconds = alarmConfig.snoozeDurationMinutes * 60
        } else {
            // No more snoozes allowed, move to Wudu
            currentStage = FajrRoutineStage.WUDU
        }
    }

    // Handle Start Wudu
    fun handleStartWudu() {
        stopAlarmSound()
        viewModel.cancelSnooze()
        isSnoozeActive = false
        currentStage = FajrRoutineStage.WUDU
    }

    // Handle Wudu Finished -> Move to Prayer Mat
    fun handleWuduFinished() {
        currentStage = FajrRoutineStage.PRAYER_MAT
    }

    // Handle Prayer Mat Recognized -> Move to Success / Pray
    fun handlePrayerMatSuccess() {
        stopAlarmSound()
        viewModel.cancelSnooze()
        currentStage = FajrRoutineStage.PRAY
    }

    // Handle Emergency Stop
    fun handleEmergencyStop() {
        stopAlarmSound()
        viewModel.cancelSnooze()
        onDismissSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07111E),
                        Color(0xFF0F2034),
                        Color(0xFF133630),
                        Color(0xFF0A1F18)
                    )
                )
            )
            .testTag("fajr_alarm_routine_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 4-Step Routine Progress Header (WAKE → WUDU → PRAYER MAT → PRAY)
            FajrRoutineProgressBar(
                currentStage = currentStage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Dynamic Routine Stage Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isSnoozeActive) {
                    SnoozeCountdownView(
                        remainingSeconds = snoozeRemainingSeconds,
                        snoozeCount = settings.currentSnoozeCount,
                        maxSnoozes = alarmConfig.maxSnoozes,
                        onTick = { snoozeRemainingSeconds = (snoozeRemainingSeconds - 1).coerceAtLeast(0) },
                        onWakeNow = { handleStartWudu() }
                    )
                } else {
                    AnimatedContent(
                        targetState = currentStage,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 4 })
                                .togetherWith(fadeOut(animationSpec = tween(200)))
                        },
                        label = "fajr_routine_stage_transition"
                    ) { stage ->
                        when (stage) {
                            FajrRoutineStage.WAKE -> {
                                val fajrFormatted = todaySchedule?.let {
                                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it.fajrMillis))
                                } ?: "05:15 AM"
                                WakeStageView(
                                    currentTime = currentTimeFormatted,
                                    fajrTime = fajrFormatted,
                                    cityName = settings.cityName,
                                    snoozeDurationMinutes = alarmConfig.snoozeDurationMinutes,
                                    maxSnoozes = alarmConfig.maxSnoozes,
                                    currentSnoozeCount = settings.currentSnoozeCount,
                                    onStartWudu = { handleStartWudu() },
                                    onSnooze = { handleSnooze() },
                                    onEmergencyStop = { showEmergencyDialog = true }
                                )
                            }
                            FajrRoutineStage.WUDU -> {
                                WuduStageView(
                                    durationMinutes = alarmConfig.wuduTimerMinutes,
                                    isGuideEnabledInitial = alarmConfig.isWuduGuideEnabled,
                                    isDetailedInitial = alarmConfig.isWuduGuideDetailed,
                                    onWuduCompleted = { handleWuduFinished() },
                                    onEmergencyStop = { showEmergencyDialog = true }
                                )
                            }
                            FajrRoutineStage.PRAYER_MAT -> {
                                if (isFallbackActive) {
                                    FallbackChallengeContainer(
                                        challengeType = fallbackChallengeType,
                                        difficulty = alarmConfig.difficulty,
                                        onSolved = { handlePrayerMatSuccess() },
                                        onBackToCamera = { isFallbackActive = false },
                                        onEmergencyStop = { showEmergencyDialog = true }
                                    )
                                } else {
                                    PrayerMatStageView(
                                        prayerMatProfile = alarmConfig.prayerMatProfile,
                                        requiredConsecutiveFrames = alarmConfig.requiredConsecutiveFrames,
                                        confidenceThreshold = alarmConfig.confidenceThreshold,
                                        onRecognized = { handlePrayerMatSuccess() },
                                        onRequestFallback = { isFallbackActive = true },
                                        onOpenCalibration = { showCalibrationDialog = true },
                                        onEmergencyStop = { showEmergencyDialog = true }
                                    )
                                }
                            }
                            FajrRoutineStage.PRAY -> {
                                PrayerSuccessStageView(
                                    isFajrPrayed = isFajrMarkedAsPrayed,
                                    onMarkPrayed = {
                                        isFajrMarkedAsPrayed = true
                                        viewModel.recordPrayerStatus(PrayerName.FAJR, PrayerStatus.ON_TIME)
                                    },
                                    onDone = onDismissSuccess
                                )
                            }
                        }
                    }
                }
            }
        }

        // Emergency Stop Confirmation Dialog
        if (showEmergencyDialog) {
            AlertDialog(
                onDismissRequest = { showEmergencyDialog = false },
                title = {
                    Text(
                        text = "Stop Alarm?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        text = "This will silence the Fajr alarm. You can continue your morning routine whenever you are ready.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEmergencyDialog = false
                            handleEmergencyStop()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_emergency_stop_btn")
                    ) {
                        Text("Stop Alarm", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEmergencyDialog = false },
                        modifier = Modifier.testTag("cancel_emergency_stop_btn")
                    ) {
                        Text("Keep Alarm")
                    }
                }
            )
        }

        // Multi-Angle Calibration / Registration Dialog
        if (showCalibrationDialog) {
            PrayerMatRegistrationDialog(
                currentProfile = alarmConfig.prayerMatProfile,
                onDismiss = { showCalibrationDialog = false },
                onProfileSaved = { newProfile ->
                    val updated = alarmConfig.copy(prayerMatProfile = newProfile)
                    viewModel.updateFajrAlarmConfig(updated)
                    showCalibrationDialog = false
                }
            )
        }
    }
}

/**
 * 4-Step Visual Routine Progress Header
 * WAKE (1) → WUDU (2) → PRAYER MAT (3) → PRAY (4)
 */
@Composable
fun FajrRoutineProgressBar(
    currentStage: FajrRoutineStage,
    modifier: Modifier = Modifier
) {
    val stages = FajrRoutineStage.values()

    Card(
        modifier = modifier.testTag("fajr_routine_progress_bar"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEachIndexed { index, stage ->
                val isCurrent = stage == currentStage
                val isCompleted = stage.stepNumber < currentStage.stepNumber
                val badgeColor = when {
                    isCurrent -> Color(0xFFE5A93C)
                    isCompleted -> Color(0xFF22C55E)
                    else -> Color.White.copy(alpha = 0.3f)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = if (isCurrent) 1f else if (isCompleted) 0.9f else 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${stage.stepNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.Black else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = stage.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrent) Color(0xFFE5A93C) else if (isCompleted) Color(0xFF22C55E) else Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )

                    if (index < stages.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (isCompleted) Color(0xFF22C55E) else Color.White.copy(alpha = 0.15f))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * Stage 1: WAKE View
 * Elegant, uncluttered dawn interface showing Fajr time, current clock, and primary actions.
 */
@Composable
fun WakeStageView(
    currentTime: String,
    fajrTime: String,
    cityName: String,
    snoozeDurationMinutes: Int,
    maxSnoozes: Int,
    currentSnoozeCount: Int,
    onStartWudu: () -> Unit,
    onSnooze: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    val canSnooze = snoozeDurationMinutes > 0 && maxSnoozes > 0 && currentSnoozeCount < maxSnoozes
    val snoozesRemaining = (maxSnoozes - currentSnoozeCount).coerceAtLeast(0)

    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    val sunScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
            .testTag("wake_stage_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Dawn Visual Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(sunScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE5A93C).copy(alpha = 0.4f),
                                Color(0xFFE5A93C).copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFE5A93C).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFE5A93C),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "الصلاة خير من النوم",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Prayer is better than sleep",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Clock & Fajr Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentTime.isNotEmpty()) currentTime else "05:15 AM",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE5A93C),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fajr begins at $fajrTime • $cityName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                if (maxSnoozes > 0 && snoozeDurationMinutes > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (canSnooze) Color(0xFFE5A93C).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (canSnooze) "Snooze: $snoozesRemaining of $maxSnoozes remaining" else "No snoozes remaining — Time to begin",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canSnooze) Color(0xFFE5A93C) else Color(0xFFFF6B6B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Primary Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [ START WUDU ]
            Button(
                onClick = onStartWudu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("start_wudu_primary_btn"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5A93C),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "START WUDU",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // [ SNOOZE ]
            if (canSnooze) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("snooze_alarm_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.4f)))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Snooze (${snoozeDurationMinutes}m • $snoozesRemaining left)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Subtle Emergency Stop
            TextButton(
                onClick = onEmergencyStop,
                modifier = Modifier.testTag("emergency_stop_alarm_btn")
            ) {
                Text(
                    text = "Stop alarm",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Snooze Countdown View
 * Calm timer display while snoozed, with instant option to wake up and start Wudu.
 */
@Composable
fun SnoozeCountdownView(
    remainingSeconds: Int,
    snoozeCount: Int,
    maxSnoozes: Int,
    onTick: () -> Unit,
    onWakeNow: () -> Unit
) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            onTick()
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("snooze_countdown_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5A93C).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Snooze,
                    contentDescription = null,
                    tint = Color(0xFFE5A93C),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Snoozed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Resting gently before starting Wudu",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5A93C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Snooze $snoozeCount of $maxSnoozes used",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Button(
            onClick = onWakeNow,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("snooze_wake_now_btn"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
        ) {
            Icon(Icons.Default.WaterDrop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("I'M UP • START WUDU", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

/**
 * Stage 2: WUDU View (Dedicated Wudu Mode)
 * Calm 5-minute countdown, optional authentic Sunnah Wudu educational guide, early finish button.
 */
@Composable
fun WuduStageView(
    durationMinutes: Int,
    isGuideEnabledInitial: Boolean,
    isDetailedInitial: Boolean,
    onWuduCompleted: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    val totalSeconds = durationMinutes * 60
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var isTimerPaused by remember { mutableStateOf(false) }
    var isGuideVisible by remember { mutableStateOf(isGuideEnabledInitial) }
    var isDetailedGuide by remember { mutableStateOf(isDetailedInitial) }

    LaunchedEffect(isTimerPaused) {
        while (!isTimerPaused && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val progress = (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)

    val wuduSteps = remember {
        listOf(
            WuduStepItem(
                stepNumber = 1,
                title = "Niyyah & Bismillah",
                arabicPhrase = "بِسْمِ اللَّهِ",
                shortSummary = "Form the sincere intention in your heart and say Bismillah.",
                detailedDescription = "Make the intention in your heart to purify yourself for prayer to Allah, and begin by reciting Bismillah (In the Name of Allah).",
                reference = "Sunan Abi Dawud"
            ),
            WuduStepItem(
                stepNumber = 2,
                title = "Wash Hands (3x)",
                arabicPhrase = "غَسْلُ الْكَفَّيْنِ",
                shortSummary = "Wash both hands up to the wrists thoroughly three times.",
                detailedDescription = "Wash your hands thoroughly including between fingers and around wrist creases three times.",
                reference = "Sahih Al-Bukhari & Muslim"
            ),
            WuduStepItem(
                stepNumber = 3,
                title = "Rinse Mouth & Nose (3x)",
                arabicPhrase = "الْمَضْمَضَةُ وَالاسْتِنْشَاقُ",
                shortSummary = "Rinse the mouth and sniff water into the nostrils three times.",
                detailedDescription = "Take water into your mouth, swirl it, and sniff water into your nose with your right hand, then blow it out using your left hand.",
                reference = "Sahih Al-Bukhari"
            ),
            WuduStepItem(
                stepNumber = 4,
                title = "Wash Face (3x)",
                arabicPhrase = "غَسْلُ الْوَجْهِ",
                shortSummary = "Wash the entire face from hairline to chin, ear to ear.",
                detailedDescription = "Wash the face completely three times from the normal hairline to the bottom of the chin, and from earlobe to earlobe.",
                reference = "Surah Al-Ma'idah 5:6"
            ),
            WuduStepItem(
                stepNumber = 5,
                title = "Wash Arms to Elbows (3x)",
                arabicPhrase = "غَسْلُ الْيَدَيْنِ إِلَى الْمِرْفَقَيْنِ",
                shortSummary = "Wash right arm including elbow 3x, then left arm 3x.",
                detailedDescription = "Wash the right forearm thoroughly from fingertips up to and including the elbow three times, then repeat for the left arm.",
                reference = "Sahih Muslim"
            ),
            WuduStepItem(
                stepNumber = 6,
                title = "Wipe Head & Ears (1x)",
                arabicPhrase = "مَسْحُ الرَّأْسِ وَالأُذُنَيْنِ",
                shortSummary = "Wipe over head from front to back, and wipe ears once.",
                detailedDescription = "Pass wet hands over your hair from the front hairline to the back of the neck and return, then wipe inside and back of both ears with index fingers and thumbs once.",
                reference = "Sahih Al-Bukhari"
            ),
            WuduStepItem(
                stepNumber = 7,
                title = "Wash Feet to Ankles (3x)",
                arabicPhrase = "غَسْلُ الرِّجْلَيْنِ إِلَى الْكَعْبَيْنِ",
                shortSummary = "Wash right foot up to ankle 3x, then left foot 3x.",
                detailedDescription = "Wash the right foot completely up to and including the ankles, washing between toes, three times. Repeat for the left foot.",
                reference = "Sahih Muslim"
            ),
            WuduStepItem(
                stepNumber = 8,
                title = "Dua After Wudu",
                arabicPhrase = "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                shortSummary = "Recite the Shahadah after completing Wudu.",
                detailedDescription = "\"I bear witness that none has the right to be worshipped but Allah alone, and Muhammad is His slave and Messenger. O Allah, make me of those who repent and purify themselves.\"",
                reference = "Sahih Muslim & Jami` at-Tirmidhi"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .testTag("wudu_stage_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Make Wudu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Take your time for complete, mindful purification",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Circular Timer & Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF38BDF8),
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeWidth = 8.dp
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (remainingSeconds > 0) Color.White else Color(0xFF22C55E)
                        )
                        Text(
                            text = if (remainingSeconds > 0) "remaining" else "Ready when you are",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "The timer is not proof that Wudu occurred — take all the time you need.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Optional Educational Wudu Guide
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isGuideVisible = !isGuideVisible }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFFE5A93C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sunnah Wudu Guide",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5A93C)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isGuideVisible) {
                            FilterChip(
                                selected = isDetailedGuide,
                                onClick = { isDetailedGuide = !isDetailedGuide },
                                label = {
                                    Text(
                                        text = if (isDetailedGuide) "Detailed" else "Short",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE5A93C).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFE5A93C)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Icon(
                            imageVector = if (isGuideVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                if (isGuideVisible) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(wuduSteps) { step ->
                            WuduStepCard(step = step, isDetailed = isDetailedGuide)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap above to view the step-by-step Sunnah Wudu guide",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onWuduCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("finished_wudu_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (remainingSeconds > 0) "I'VE FINISHED WUDU" else "SCAN PRAYER MAT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            TextButton(
                onClick = onEmergencyStop,
                modifier = Modifier.testTag("wudu_emergency_stop_btn")
            ) {
                Text(
                    text = "Stop alarm",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
fun WuduStepCard(step: WuduStepItem, isDetailed: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.07f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${step.stepNumber}. ${step.title}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                if (step.arabicPhrase != null) {
                    Text(
                        text = step.arabicPhrase,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5A93C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isDetailed) step.detailedDescription else step.shortSummary,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            if (isDetailed && step.reference.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Source: ${step.reference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Stage 3: PRAYER MAT View (Find your prayer mat)
 * Live camera computer vision scanner with guidance, verification counter, calibration & fallbacks.
 */
@Composable
fun PrayerMatStageView(
    prayerMatProfile: com.example.data.model.PrayerMatProfile,
    requiredConsecutiveFrames: Int,
    confidenceThreshold: Float,
    onRecognized: () -> Unit,
    onRequestFallback: () -> Unit,
    onOpenCalibration: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_mat_stage_view"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Find your prayer mat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Walk to your prayer mat and point the live camera at it",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live CameraX Scanner Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                PrayerMatCameraScannerView(
                    prayerMatProfile = prayerMatProfile,
                    requiredConsecutiveFrames = requiredConsecutiveFrames,
                    confidenceThreshold = confidenceThreshold,
                    onPrayerMatRecognized = onRecognized,
                    onFallbackRequested = onRequestFallback,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Auxiliary Actions (Calibrate / Alternative Challenge / Emergency Stop)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpenCalibration,
                    modifier = Modifier.testTag("recalibrate_mat_btn")
                ) {
                    Text(
                        text = if (prayerMatProfile.isRegistered) "Recalibrate Rug" else "Register Rug",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE5A93C)
                    )
                }

                TextButton(
                    onClick = onRequestFallback,
                    modifier = Modifier.testTag("use_backup_challenge_btn")
                ) {
                    Text(
                        text = "Use Another Challenge",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                TextButton(
                    onClick = onEmergencyStop,
                    modifier = Modifier.testTag("mat_emergency_stop_btn")
                ) {
                    Text(
                        text = "Stop alarm",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

/**
 * Fallback Challenge Container (Math, Memory, Shake)
 */
@Composable
fun FallbackChallengeContainer(
    challengeType: FajrChallengeType,
    difficulty: ChallengeDifficulty,
    onSolved: () -> Unit,
    onBackToCamera: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fallback_challenge_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackToCamera) {
                Text("← Back to Camera", color = Color(0xFFE5A93C))
            }
            Text(
                text = "Alternative Challenge",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (challengeType) {
                FajrChallengeType.MATH -> MathChallengeView(onSolved = onSolved)
                FajrChallengeType.SHAKE -> ShakeChallengeView(targetShakes = 25, onFinished = onSolved)
                FajrChallengeType.MEMORY -> MemorySequenceChallengeView(onSolved = onSolved)
                else -> MathChallengeView(onSolved = onSolved)
            }
        }

        TextButton(
            onClick = onEmergencyStop,
            modifier = Modifier.testTag("fallback_emergency_stop_btn")
        ) {
            Text("Stop alarm", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
        }
    }
}

@Composable
fun MathChallengeView(onSolved: () -> Unit) {
    var solvedCount by remember { mutableIntStateOf(0) }
    val totalRequired = 3

    var currentProblem by remember(solvedCount) {
        val a = Random.nextInt(12, 45)
        val b = Random.nextInt(7, 35)
        val correct = a + b
        val options = listOf(
            correct,
            correct + Random.nextInt(1, 4),
            correct - Random.nextInt(1, 4),
            correct + 10
        ).shuffled()
        mutableStateOf(MathProblem("$a + $b = ?", options, options.indexOf(correct)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Solve Math ($solvedCount/$totalRequired)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentProblem.question,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5A93C)
            )

            Spacer(modifier = Modifier.height(20.dp))

            currentProblem.options.forEachIndexed { index, option ->
                Button(
                    onClick = {
                        if (index == currentProblem.correctIndex) {
                            if (solvedCount + 1 >= totalRequired) {
                                onSolved()
                            } else {
                                solvedCount++
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(48.dp)
                        .testTag("math_option_$index"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Text("$option", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShakeChallengeView(targetShakes: Int = 25, onFinished: () -> Unit) {
    val context = LocalContext.current
    var shakeCount by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        var lastTime: Long = 0

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val curTime = System.currentTimeMillis()
                val diffTime = curTime - lastTime

                if (diffTime > 100) {
                    lastTime = curTime
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = sqrt(((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)).toDouble()) / diffTime * 10000

                    if (speed > 800) {
                        shakeCount++
                        if (shakeCount >= targetShakes) {
                            onFinished()
                        }
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Vibration,
                contentDescription = null,
                tint = Color(0xFFE5A93C),
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Shake Phone Briskly",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Physical movement to awaken your body",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "$shakeCount / $targetShakes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF22C55E)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { (shakeCount.toFloat() / targetShakes).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF22C55E),
                trackColor = Color.White.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    shakeCount += 5
                    if (shakeCount >= targetShakes) {
                        onFinished()
                    }
                },
                modifier = Modifier.testTag("manual_shake_tap_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            ) {
                Text("Tap to Shake (+5)")
            }
        }
    }
}

@Composable
fun MemorySequenceChallengeView(onSolved: () -> Unit) {
    val totalSteps = 4
    val sequence = remember { List(totalSteps) { Random.nextInt(0, 4) } }
    var userTaps = remember { mutableStateListOf<Int>() }
    var isFailed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Memory Sequence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap in sequence: ${sequence.map { it + 1 }.joinToString(" → ")}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE5A93C),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (btnIndex in 0..3) {
                    Button(
                        onClick = {
                            if (userTaps.size < sequence.size) {
                                if (sequence[userTaps.size] == btnIndex) {
                                    userTaps.add(btnIndex)
                                    if (userTaps.size == sequence.size) {
                                        onSolved()
                                    }
                                } else {
                                    userTaps.clear()
                                    isFailed = true
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .testTag("memory_btn_$btnIndex"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("${btnIndex + 1}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Progress: ${userTaps.size} / $totalSteps completed",
                style = MaterialTheme.typography.bodySmall,
                color = if (isFailed) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Stage 4: SUCCESS & PRAY View
 * "You're up. 🤍"
 * Actions: [ PRAY FAJR ], [ READ QURAN ], [ ADHKAR ], [ TASBIH ], [ MARK FAJR AS PRAYED ]
 */
@Composable
fun PrayerSuccessStageView(
    isFajrPrayed: Boolean,
    onMarkPrayed: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("prayer_success_stage_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Spiritual Success Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "You're up. 🤍",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Alhamdulillah. You've completed the wake-up routine.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Text(
                text = "May Allah accept your Fajr prayer and illuminate your day.",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFE5A93C),
                textAlign = TextAlign.Center
            )
        }

        // Post-Routine Action Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Fajr Prayer & Remembrance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Mark Fajr as Prayed Button
                Button(
                    onClick = onMarkPrayed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("mark_fajr_prayed_routine_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFajrPrayed) Color(0xFF22C55E) else Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isFajrPrayed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (isFajrPrayed) Color.White else Color(0xFF22C55E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFajrPrayed) "Fajr Marked as Prayed ✓" else "Mark Fajr as Prayed",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Note: Fajr is not automatically marked as prayed until you complete your prayer.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
        }

        // Return / Continue Button
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("return_home_routine_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C), contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ENTER MAIN APP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
