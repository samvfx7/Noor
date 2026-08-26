package com.samvfx7.noor.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samvfx7.noor.service.QiblaSensorManager
import com.samvfx7.noor.ui.theme.EmeraldPrimaryLight
import com.samvfx7.noor.ui.theme.GoldSecondaryLight
import com.samvfx7.noor.ui.viewmodel.PrayerViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    val settings = viewModel.getSettingsManager()

    val sensorManager = remember { QiblaSensorManager(context) }
    val compassState by sensorManager.compassState.collectAsState()

    DisposableEffect(qiblaBearing) {
        sensorManager.start(qiblaBearing)
        onDispose {
            sensorManager.stop()
        }
    }

    // Gentle haptic feedback when aligned
    LaunchedEffect(compassState.isAlignedWithKaaba) {
        if (compassState.isAlignedWithKaaba) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qibla Compass", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("qibla_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${settings.cityName}, ${settings.countryName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qibla: ${qiblaBearing.toInt()}° from North",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Compass Dial View
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .testTag("qibla_compass_dial"),
                contentAlignment = Alignment.Center
            ) {
                // Dial Background Ring
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                val goldColor = GoldSecondaryLight

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 16.dp.toPx()

                    drawCircle(
                        color = outlineColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Cardinal tick marks
                    for (i in 0 until 360 step 30) {
                        val rad = Math.toRadians(i.toDouble())
                        val tickLen = if (i % 90 == 0) 14.dp.toPx() else 8.dp.toPx()
                        val start = Offset(
                            (center.x + (radius - tickLen) * sin(rad)).toFloat(),
                            (center.y - (radius - tickLen) * cos(rad)).toFloat()
                        )
                        val end = Offset(
                            (center.x + radius * sin(rad)).toFloat(),
                            (center.y - radius * cos(rad)).toFloat()
                        )
                        drawLine(
                            color = if (i == 0) Color.Red else outlineColor,
                            start = start,
                            end = end,
                            strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                        )
                    }
                }

                // Rotating Needle
                val needleRotation = compassState.angleDifferenceDeg
                val isAligned = compassState.isAlignedWithKaaba

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.rotate(-needleRotation)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Qibla Direction",
                        tint = if (isAligned) EmeraldPrimaryLight else goldColor,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (isAligned) EmeraldPrimaryLight else Color.Gray)
                    )
                }

                // Center Kaaba Emblem
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isAligned) EmeraldPrimaryLight else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🕋",
                        fontSize = 24.sp
                    )
                }
            }

            // Alignment Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (compassState.isAlignedWithKaaba) EmeraldPrimaryLight.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (compassState.isAlignedWithKaaba) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Aligned",
                            tint = EmeraldPrimaryLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Facing the Holy Kaaba Directly",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimaryLight
                        )
                    } else {
                        val diff = compassState.angleDifferenceDeg.toInt()
                        val turnText = if (diff > 0) "Turn right $diff°" else "Turn left ${-diff}°"
                        Text(
                            text = turnText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Calibration Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keep device flat. If heading is inaccurate, wave your device in a figure-8 motion to calibrate magnetic sensors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
