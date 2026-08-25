package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FajrAlarmConfig
import com.example.data.model.FajrChallengeType
import com.example.service.PrayerMatRecognitionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Noor", appName)
  }

  @Test
  fun `prayer mat recognition engine serialization test`() {
    val dummyCells = (1..9).map {
      PrayerMatRecognitionEngine.SpatialCell(
        hueHistogram = FloatArray(12) { 0.08f },
        avgSaturation = 0.65f,
        avgBrightness = 0.55f,
        edgeDensity = 0.20f
      )
    }
    val sampleSig = PrayerMatRecognitionEngine.PrayerMatSignature(
      id = "test_sample_1",
      cells = dummyCells,
      dominantHueBins = intArrayOf(4, 5, 1, 8),
      luminanceMatrix = FloatArray(64) { 0.5f }
    )

    val json = PrayerMatRecognitionEngine.serializeSignatures(listOf(sampleSig))
    assertTrue(json.isNotEmpty())

    val deserialized = PrayerMatRecognitionEngine.deserializeSignatures(json)
    assertEquals(1, deserialized.size)
    assertEquals("test_sample_1", deserialized[0].id)

    val confidence = PrayerMatRecognitionEngine.calculateMatchConfidence(sampleSig, deserialized)
    assertTrue("Self match confidence should be >= 0.85", confidence >= 0.85f)
  }

  @Test
  fun `fajr alarm config default challenge type is prayer mat recognition`() {
    val config = FajrAlarmConfig()
    assertEquals(FajrChallengeType.PRAYER_MAT_RECOGNITION, config.challengeType)
    assertEquals(FajrChallengeType.MATH, config.fallbackChallengeType)
    assertEquals(6, config.requiredConsecutiveFrames)
  }
}
