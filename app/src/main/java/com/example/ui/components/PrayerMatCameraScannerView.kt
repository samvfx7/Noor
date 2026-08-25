package com.example.ui.components

import android.Manifest
import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.PrayerMatProfile
import com.example.service.PrayerMatRecognitionEngine
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerMatCameraScannerView(
    prayerMatProfile: PrayerMatProfile,
    requiredConsecutiveFrames: Int = 6,
    confidenceThreshold: Float = 0.65f,
    onPrayerMatRecognized: () -> Unit,
    onFallbackRequested: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentConfidence by remember { mutableFloatStateOf(0f) }
    var consecutiveFramesMatched by remember { mutableIntStateOf(0) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var hasCompletedTrigger by remember { mutableStateOf(false) }

    // Deserialize reference signatures once
    val refSignatures = remember(prayerMatProfile.signaturesJson) {
        val list = PrayerMatRecognitionEngine.deserializeSignatures(prayerMatProfile.signaturesJson)
        if (list.isEmpty()) {
            // Default velvet carpet reference signature if not calibrated
            val dummyCells = (1..9).map {
                PrayerMatRecognitionEngine.SpatialCell(
                    hueHistogram = FloatArray(12) { 0.08f },
                    avgSaturation = 0.60f,
                    avgBrightness = 0.50f,
                    edgeDensity = 0.20f
                )
            }
            listOf(
                PrayerMatRecognitionEngine.PrayerMatSignature(
                    cells = dummyCells,
                    dominantHueBins = intArrayOf(4, 5, 1, 8),
                    luminanceMatrix = FloatArray(64) { 0.5f }
                )
            )
        } else {
            list
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Camera Permission",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Noor analyzes the live camera feed on-device to recognize your physical prayer mat.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { cameraPermissionState.launchPermissionRequest() },
                modifier = Modifier.testTag("request_camera_perm_button")
            ) {
                Text("Enable Camera")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onPrayerMatRecognized,
                modifier = Modifier.testTag("manual_test_prayermat_match_button")
            ) {
                Text("Test Match (Bypass for Testing)")
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            try {
                                if (hasCompletedTrigger) return@setAnalyzer

                                val liveSig = PrayerMatRecognitionEngine.extractSignatureFromImageProxy(imageProxy)
                                if (liveSig != null) {
                                    val conf = PrayerMatRecognitionEngine.calculateMatchConfidence(liveSig, refSignatures)
                                    currentConfidence = conf

                                    if (conf >= confidenceThreshold) {
                                        consecutiveFramesMatched++
                                        if (consecutiveFramesMatched >= requiredConsecutiveFrames && !hasCompletedTrigger) {
                                            hasCompletedTrigger = true
                                            onPrayerMatRecognized()
                                        }
                                    } else {
                                        consecutiveFramesMatched = (consecutiveFramesMatched - 1).coerceAtLeast(0)
                                    }
                                }
                            } finally {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            val camera: Camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraControl = camera.cameraControl
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Dynamic Reticle Border Color based on match confidence
            val isMatching = currentConfidence >= confidenceThreshold
            val animatedBorderColor by animateColorAsState(
                targetValue = if (isMatching) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                animationSpec = tween(300), label = "border_color"
            )

            // Prayer Mat Scanner Target Reticle (Arch Motif)
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 320.dp)
                    .border(
                        width = if (isMatching) 3.5.dp else 2.dp,
                        color = animatedBorderColor,
                        shape = RoundedCornerShape(topStart = 72.dp, topEnd = 72.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
            )

            // Top Status Bar Overlay (Live Feed Indicator & Flashlight)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Stream Active Chip
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isMatching) Color(0xFF22C55E) else Color(0xFFE5A93C))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMatching) "PRAYER MAT DETECTED" else "SCANNING LIVE FEED",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Flashlight / Torch Toggle
                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flashlight",
                        tint = if (isTorchOn) Color(0xFFFFD54F) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Real-time Recognition Analysis HUD
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.82f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Point camera at prayer rug",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "${(currentConfidence * 100).toInt()}% Match",
                                color = if (isMatching) Color(0xFF22C55E) else Color(0xFFFFB74D),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Consecutive Frame Verification Bar
                        val progress = (consecutiveFramesMatched.toFloat() / requiredConsecutiveFrames.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF22C55E),
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Hold steady: $consecutiveFramesMatched / $requiredConsecutiveFrames consecutive frames verified",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    onFallbackRequested?.let { onFallback ->
                        OutlinedButton(
                            onClick = onFallback,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("use_fallback_challenge_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Use Fallback Challenge", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onPrayerMatRecognized,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_test_prayermat_match_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Test Match (Bypass)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
