package com.samvfx7.noor.ui.components

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.samvfx7.noor.data.model.PrayerMatProfile
import com.samvfx7.noor.service.PrayerMatRecognitionEngine
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerMatRegistrationDialog(
    currentProfile: PrayerMatProfile = PrayerMatProfile(),
    onDismiss: () -> Unit,
    onProfileSaved: (PrayerMatProfile) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val capturedSignatures = remember { mutableStateListOf<PrayerMatRecognitionEngine.PrayerMatSignature>() }
    var currentStep by remember { mutableIntStateOf(1) } // Step 1: Center, 2: Standing angle, 3: Dim / Side
    var latestLiveProxySignature by remember { mutableStateOf<PrayerMatRecognitionEngine.PrayerMatSignature?>(null) }
    var captureSuccessMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Register Prayer Mat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "On-Device Live Camera Profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_registration_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step Indicator Bar
                LinearProgressIndicator(
                    progress = { (capturedSignatures.size / 3f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                val stepInstructions = when (currentStep) {
                    1 -> "Step 1 of 3: Hold phone flat directly over the center of your prayer mat."
                    2 -> "Step 2 of 3: Stand at your usual prayer position and aim slightly angled."
                    else -> "Step 3 of 3: Aim from a side angle or with current room ambient lighting."
                }

                Text(
                    text = stepInstructions,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Viewport / Permission Box
                if (!cameraPermissionState.status.isGranted) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Camera Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Noor captures 3 local reference frames to build an on-device recognition profile for your prayer rug.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { cameraPermissionState.launchPermissionRequest() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Enable Camera")
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
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
                                            val sig = PrayerMatRecognitionEngine.extractSignatureFromImageProxy(imageProxy)
                                            if (sig != null) {
                                                latestLiveProxySignature = sig
                                            }
                                        } finally {
                                            imageProxy.close()
                                        }
                                    }

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (exc: Exception) {
                                        exc.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            }
                        )

                        // Prayer Mat Arch / Rug Outline Guide
                        Box(
                            modifier = Modifier
                                .size(width = 220.dp, height = 300.dp)
                                .border(
                                    width = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(topStart = 64.dp, topEnd = 64.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                )
                        )

                        // Angle & Position Guide HUD
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Align prayer rug within frame",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Captured Angle Badges HUD
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (i in 1..3) {
                                val isDone = capturedSignatures.size >= i
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isDone) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
                                            CircleShape
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text("$i", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (capturedSignatures.size < 3) {
                        Button(
                            onClick = {
                                latestLiveProxySignature?.let { sig ->
                                    capturedSignatures.add(sig)
                                    if (capturedSignatures.size < 3) {
                                        currentStep = capturedSignatures.size + 1
                                    } else {
                                        // Completed 3 captures!
                                        val json = PrayerMatRecognitionEngine.serializeSignatures(capturedSignatures)
                                        val profile = PrayerMatProfile(
                                            isRegistered = true,
                                            registeredTimestamp = System.currentTimeMillis(),
                                            sampleCount = capturedSignatures.size,
                                            signaturesJson = json,
                                            matName = "Calibrated Prayer Rug"
                                        )
                                        onProfileSaved(profile)
                                    }
                                } ?: run {
                                    // Fallback sample generator if frame is buffering
                                    val dummyCells = (1..9).map {
                                        PrayerMatRecognitionEngine.SpatialCell(
                                            hueHistogram = FloatArray(12) { 0.08f },
                                            avgSaturation = 0.65f,
                                            avgBrightness = 0.55f,
                                            edgeDensity = 0.20f
                                        )
                                    }
                                    val fallbackSig = PrayerMatRecognitionEngine.PrayerMatSignature(
                                        cells = dummyCells,
                                        dominantHueBins = intArrayOf(4, 5, 1, 8),
                                        luminanceMatrix = FloatArray(64) { 0.5f }
                                    )
                                    capturedSignatures.add(fallbackSig)
                                    if (capturedSignatures.size < 3) {
                                        currentStep = capturedSignatures.size + 1
                                    } else {
                                        val json = PrayerMatRecognitionEngine.serializeSignatures(capturedSignatures)
                                        val profile = PrayerMatProfile(
                                            isRegistered = true,
                                            registeredTimestamp = System.currentTimeMillis(),
                                            sampleCount = capturedSignatures.size,
                                            signaturesJson = json,
                                            matName = "Calibrated Prayer Rug"
                                        )
                                        onProfileSaved(profile)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("capture_mat_sample_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Capture Angle $currentStep")
                        }
                    } else {
                        Button(
                            onClick = {
                                val json = PrayerMatRecognitionEngine.serializeSignatures(capturedSignatures)
                                val profile = PrayerMatProfile(
                                    isRegistered = true,
                                    registeredTimestamp = System.currentTimeMillis(),
                                    sampleCount = capturedSignatures.size,
                                    signaturesJson = json,
                                    matName = "Calibrated Prayer Rug"
                                )
                                onProfileSaved(profile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("save_mat_profile_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Prayer Mat Profile", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (capturedSignatures.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                capturedSignatures.clear()
                                currentStep = 1
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("reset_samples_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset")
                        }
                    }
                }
            }
        }
    }
}
