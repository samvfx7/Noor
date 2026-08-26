package com.samvfx7.noor.service

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.camera.core.ImageProxy
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * On-Device Computer Vision Engine for Prayer Mat Camera Recognition.
 * Operates 100% locally on-device without any cloud connectivity, external models, or server calls.
 * Analyzes live CameraX ImageProxy frames for multi-region spatial color distribution,
 * edge/texture density, and dominant pattern layouts.
 */
object PrayerMatRecognitionEngine {

    const val DEFAULT_CONFIDENCE_THRESHOLD = 0.65f
    const val REQUIRED_CONSECUTIVE_FRAMES = 6

    data class SpatialCell(
        val hueHistogram: FloatArray, // 12 bins
        val avgSaturation: Float,
        val avgBrightness: Float,
        val edgeDensity: Float
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as SpatialCell
            return hueHistogram.contentEquals(other.hueHistogram) &&
                    avgSaturation == other.avgSaturation &&
                    avgBrightness == other.avgBrightness &&
                    edgeDensity == other.edgeDensity
        }

        override fun hashCode(): Int {
            var result = hueHistogram.contentHashCode()
            result = 31 * result + avgSaturation.hashCode()
            result = 31 * result + avgBrightness.hashCode()
            result = 31 * result + edgeDensity.hashCode()
            return result
        }
    }

    data class PrayerMatSignature(
        val id: String = System.currentTimeMillis().toString(),
        val cells: List<SpatialCell>, // 9 cells (3x3 grid)
        val dominantHueBins: IntArray, // Top 4 dominant hue bins
        val luminanceMatrix: FloatArray // 8x8 normalized luminance (64 values)
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PrayerMatSignature
            return id == other.id &&
                    cells == other.cells &&
                    dominantHueBins.contentEquals(other.dominantHueBins) &&
                    luminanceMatrix.contentEquals(other.luminanceMatrix)
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + cells.hashCode()
            result = 31 * result + dominantHueBins.contentHashCode()
            result = 31 * result + luminanceMatrix.contentHashCode()
            return result
        }
    }

    /**
     * Extracts a feature signature from a captured Bitmap during setup.
     */
    fun extractSignatureFromBitmap(bitmap: Bitmap): PrayerMatSignature {
        val width = bitmap.width
        val height = bitmap.height

        // Define Region of Interest (Center 80% of image)
        val startX = (width * 0.10f).toInt()
        val endX = (width * 0.90f).toInt()
        val startY = (height * 0.10f).toInt()
        val endY = (height * 0.90f).toInt()

        val roiWidth = endX - startX
        val roiHeight = endY - startY

        val gridCols = 3
        val gridRows = 3
        val cellWidth = roiWidth / gridCols
        val cellHeight = roiHeight / gridRows

        val cells = mutableListOf<SpatialCell>()
        val overallHueCounts = FloatArray(12) { 0f }
        val luminance8x8 = FloatArray(64) { 0f }
        val lumCount8x8 = IntArray(64) { 0 }

        // Process grid cells
        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val cStartX = startX + c * cellWidth
                val cEndX = cStartX + cellWidth
                val cStartY = startY + r * cellHeight
                val cEndY = cStartY + cellHeight

                val hueBins = FloatArray(12) { 0f }
                var totalSat = 0f
                var totalVal = 0f
                var sampleCount = 0
                var edgeSum = 0f

                val step = max(1, min(cellWidth, cellHeight) / 24)

                for (y in cStartY until cEndY step step) {
                    for (x in cStartX until cEndX step step) {
                        if (x < width && y < height) {
                            val pixel = bitmap.getPixel(x, y)
                            val rCol = AndroidColor.red(pixel)
                            val gCol = AndroidColor.green(pixel)
                            val bCol = AndroidColor.blue(pixel)

                            val hsv = FloatArray(3)
                            AndroidColor.RGBToHSV(rCol, gCol, bCol, hsv)
                            val hue = hsv[0] // 0..360
                            val sat = hsv[1] // 0..1
                            val value = hsv[2] // 0..1

                            val hueBin = ((hue % 360f) / 30f).toInt().coerceIn(0, 11)
                            hueBins[hueBin] += (0.2f + sat * 0.8f)
                            overallHueCounts[hueBin] += (0.2f + sat * 0.8f)

                            totalSat += sat
                            totalVal += value
                            sampleCount++

                            // Simple horizontal edge detection
                            if (x + step < width) {
                                val nextPixel = bitmap.getPixel(x + step, y)
                                val nextLum = (AndroidColor.red(nextPixel) * 0.299f +
                                        AndroidColor.green(nextPixel) * 0.587f +
                                        AndroidColor.blue(nextPixel) * 0.114f)
                                val curLum = (rCol * 0.299f + gCol * 0.587f + bCol * 0.114f)
                                edgeSum += abs(curLum - nextLum)
                            }

                            // 8x8 global luminance grid
                            val lCol = ((x - startX).toFloat() / roiWidth * 8).toInt().coerceIn(0, 7)
                            val lRow = ((y - startY).toFloat() / roiHeight * 8).toInt().coerceIn(0, 7)
                            val lIdx = lRow * 8 + lCol
                            val lumVal = (rCol * 0.299f + gCol * 0.587f + bCol * 0.114f) / 255f
                            luminance8x8[lIdx] += lumVal
                            lumCount8x8[lIdx]++
                        }
                    }
                }

                // Normalize cell hue histogram
                val totalHues = hueBins.sum().coerceAtLeast(0.001f)
                for (i in hueBins.indices) {
                    hueBins[i] /= totalHues
                }

                val avgSat = if (sampleCount > 0) totalSat / sampleCount else 0f
                val avgVal = if (sampleCount > 0) totalVal / sampleCount else 0f
                val edgeDens = if (sampleCount > 0) (edgeSum / sampleCount) / 255f else 0f

                cells.add(SpatialCell(hueBins, avgSat, avgVal, edgeDens))
            }
        }

        // Top 4 dominant hue bins
        val dominantBins = overallHueCounts.indices
            .sortedByDescending { overallHueCounts[it] }
            .take(4)
            .toIntArray()

        // Normalize 8x8 luminance grid
        for (i in 0 until 64) {
            if (lumCount8x8[i] > 0) {
                luminance8x8[i] /= lumCount8x8[i]
            }
        }

        return PrayerMatSignature(
            cells = cells,
            dominantHueBins = dominantBins,
            luminanceMatrix = luminance8x8
        )
    }

    /**
     * Extracts a feature signature from a live CameraX ImageProxy frame in real-time.
     */
    fun extractSignatureFromImageProxy(imageProxy: ImageProxy): PrayerMatSignature? {
        val yPlane = imageProxy.planes.getOrNull(0) ?: return null
        val uPlane = imageProxy.planes.getOrNull(1)
        val vPlane = imageProxy.planes.getOrNull(2)

        val yBuffer: ByteBuffer = yPlane.buffer
        val width = imageProxy.width
        val height = imageProxy.height
        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride

        val uBuffer = uPlane?.buffer
        val vBuffer = vPlane?.buffer
        val uvRowStride = uPlane?.rowStride ?: yRowStride
        val uvPixelStride = uPlane?.pixelStride ?: 1

        val startX = (width * 0.15f).toInt()
        val endX = (width * 0.85f).toInt()
        val startY = (height * 0.15f).toInt()
        val endY = (height * 0.85f).toInt()

        val roiWidth = endX - startX
        val roiHeight = endY - startY
        if (roiWidth <= 0 || roiHeight <= 0) return null

        val gridCols = 3
        val gridRows = 3
        val cellWidth = roiWidth / gridCols
        val cellHeight = roiHeight / gridRows

        val cells = mutableListOf<SpatialCell>()
        val overallHueCounts = FloatArray(12) { 0f }
        val luminance8x8 = FloatArray(64) { 0f }
        val lumCount8x8 = IntArray(64) { 0 }

        val step = max(2, min(cellWidth, cellHeight) / 16)

        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val cStartX = startX + c * cellWidth
                val cEndX = cStartX + cellWidth
                val cStartY = startY + r * cellHeight
                val cEndY = cStartY + cellHeight

                val hueBins = FloatArray(12) { 0f }
                var totalSat = 0f
                var totalVal = 0f
                var sampleCount = 0
                var edgeSum = 0f

                for (y in cStartY until cEndY step step) {
                    for (x in cStartX until cEndX step step) {
                        if (x < width && y < height) {
                            val yIndex = y * yRowStride + x * yPixelStride
                            if (yIndex < yBuffer.limit()) {
                                val yVal = (yBuffer.get(yIndex).toInt() and 0xFF)

                                var uVal = 128
                                var vVal = 128
                                if (uBuffer != null && vBuffer != null) {
                                    val uvIndex = (y / 2) * uvRowStride + (x / 2) * uvPixelStride
                                    if (uvIndex < uBuffer.limit() && uvIndex < vBuffer.limit()) {
                                        uVal = (uBuffer.get(uvIndex).toInt() and 0xFF)
                                        vVal = (vBuffer.get(uvIndex).toInt() and 0xFF)
                                    }
                                }

                                // Convert YUV to approximate RGB
                                val cDiff = yVal - 16
                                val dDiff = uVal - 128
                                val eDiff = vVal - 128

                                val rCol = ((298 * cDiff + 409 * eDiff + 128) shr 8).coerceIn(0, 255)
                                val gCol = ((298 * cDiff - 100 * dDiff - 208 * eDiff + 128) shr 8).coerceIn(0, 255)
                                val bCol = ((298 * cDiff + 516 * dDiff + 128) shr 8).coerceIn(0, 255)

                                val hsv = FloatArray(3)
                                AndroidColor.RGBToHSV(rCol, gCol, bCol, hsv)
                                val hue = hsv[0]
                                val sat = hsv[1]
                                val value = hsv[2]

                                val hueBin = ((hue % 360f) / 30f).toInt().coerceIn(0, 11)
                                hueBins[hueBin] += (0.2f + sat * 0.8f)
                                overallHueCounts[hueBin] += (0.2f + sat * 0.8f)

                                totalSat += sat
                                totalVal += value
                                sampleCount++

                                // Simple edge check
                                val nextYIndex = y * yRowStride + (x + step).coerceAtMost(width - 1) * yPixelStride
                                if (nextYIndex < yBuffer.limit()) {
                                    val nextYVal = (yBuffer.get(nextYIndex).toInt() and 0xFF)
                                    edgeSum += abs(yVal - nextYVal)
                                }

                                // 8x8 global luminance grid
                                val lCol = ((x - startX).toFloat() / roiWidth * 8).toInt().coerceIn(0, 7)
                                val lRow = ((y - startY).toFloat() / roiHeight * 8).toInt().coerceIn(0, 7)
                                val lIdx = lRow * 8 + lCol
                                val lumVal = yVal / 255f
                                luminance8x8[lIdx] += lumVal
                                lumCount8x8[lIdx]++
                            }
                        }
                    }
                }

                val totalHues = hueBins.sum().coerceAtLeast(0.001f)
                for (i in hueBins.indices) {
                    hueBins[i] /= totalHues
                }

                val avgSat = if (sampleCount > 0) totalSat / sampleCount else 0f
                val avgVal = if (sampleCount > 0) totalVal / sampleCount else 0f
                val edgeDens = if (sampleCount > 0) (edgeSum / sampleCount) / 255f else 0f

                cells.add(SpatialCell(hueBins, avgSat, avgVal, edgeDens))
            }
        }

        val dominantBins = overallHueCounts.indices
            .sortedByDescending { overallHueCounts[it] }
            .take(4)
            .toIntArray()

        for (i in 0 until 64) {
            if (lumCount8x8[i] > 0) {
                luminance8x8[i] /= lumCount8x8[i]
            }
        }

        return PrayerMatSignature(
            cells = cells,
            dominantHueBins = dominantBins,
            luminanceMatrix = luminance8x8
        )
    }

    /**
     * Calculates the matching confidence score (0.0 to 1.0) between a live frame signature
     * and a registered prayer mat profile (which contains multi-angle reference signatures).
     */
    fun calculateMatchConfidence(
        liveSig: PrayerMatSignature,
        referenceSignatures: List<PrayerMatSignature>
    ): Float {
        if (referenceSignatures.isEmpty()) return 0f

        var highestConfidence = 0f

        for (ref in referenceSignatures) {
            val score = compareSingleSignature(liveSig, ref)
            if (score > highestConfidence) {
                highestConfidence = score
            }
        }

        return highestConfidence.coerceIn(0f, 1.0f)
    }

    private fun compareSingleSignature(live: PrayerMatSignature, ref: PrayerMatSignature): Float {
        // 1. Spatial Grid Color Histogram Intersection (40% weight)
        var gridScoreSum = 0f
        val cellCount = min(live.cells.size, ref.cells.size)
        if (cellCount == 0) return 0f

        for (i in 0 until cellCount) {
            val liveCell = live.cells[i]
            val refCell = ref.cells[i]

            // Histogram intersection
            var intersection = 0f
            for (b in 0 until 12) {
                intersection += min(liveCell.hueHistogram[b], refCell.hueHistogram[b])
            }

            // Saturation & Value proximity
            val satDiff = abs(liveCell.avgSaturation - refCell.avgSaturation)
            val satProximity = (1f - satDiff).coerceIn(0f, 1f)

            val edgeDiff = abs(liveCell.edgeDensity - refCell.edgeDensity)
            val edgeProximity = (1f - edgeDiff * 2f).coerceIn(0f, 1f)

            val cellScore = (intersection * 0.6f + satProximity * 0.25f + edgeProximity * 0.15f)
            gridScoreSum += cellScore
        }
        val gridScore = gridScoreSum / cellCount

        // 2. Dominant Hue Overlap (30% weight)
        var dominantMatches = 0
        for (bin in live.dominantHueBins) {
            if (ref.dominantHueBins.contains(bin)) {
                dominantMatches++
            }
        }
        val dominantScore = (dominantMatches.toFloat() / 4f).coerceIn(0f, 1f)

        // 3. Structural Luminance Matrix Cosine Similarity (30% weight)
        var dotProduct = 0f
        var normLive = 0f
        var normRef = 0f
        for (i in 0 until 64) {
            val l = live.luminanceMatrix.getOrElse(i) { 0f }
            val r = ref.luminanceMatrix.getOrElse(i) { 0f }
            dotProduct += l * r
            normLive += l * l
            normRef += r * r
        }
        val cosineSim = if (normLive > 0f && normRef > 0f) {
            (dotProduct / (sqrt(normLive) * sqrt(normRef))).coerceIn(0f, 1f)
        } else {
            0.5f
        }

        return (gridScore * 0.45f + dominantScore * 0.30f + cosineSim * 0.25f)
    }

    // JSON serialization utilities for storing profiles locally on-device
    fun serializeSignatures(signatures: List<PrayerMatSignature>): String {
        val rootArray = JSONArray()
        for (sig in signatures) {
            val obj = JSONObject()
            obj.put("id", sig.id)

            val cellsArray = JSONArray()
            for (c in sig.cells) {
                val cObj = JSONObject()
                val huesArr = JSONArray()
                for (h in c.hueHistogram) huesArr.put(h.toDouble())
                cObj.put("hues", huesArr)
                cObj.put("sat", c.avgSaturation.toDouble())
                cObj.put("val", c.avgBrightness.toDouble())
                cObj.put("edge", c.edgeDensity.toDouble())
                cellsArray.put(cObj)
            }
            obj.put("cells", cellsArray)

            val domArray = JSONArray()
            for (d in sig.dominantHueBins) domArray.put(d)
            obj.put("domHues", domArray)

            val lumArray = JSONArray()
            for (l in sig.luminanceMatrix) lumArray.put(l.toDouble())
            obj.put("lum", lumArray)

            rootArray.put(obj)
        }
        return rootArray.toString()
    }

    fun deserializeSignatures(jsonStr: String): List<PrayerMatSignature> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<PrayerMatSignature>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", i.toString())

                val cellsList = mutableListOf<SpatialCell>()
                val cellsArray = obj.optJSONArray("cells")
                if (cellsArray != null) {
                    for (cIdx in 0 until cellsArray.length()) {
                        val cObj = cellsArray.getJSONObject(cIdx)
                        val huesArr = cObj.getJSONArray("hues")
                        val hues = FloatArray(12)
                        for (h in 0 until min(12, huesArr.length())) {
                            hues[h] = huesArr.getDouble(h).toFloat()
                        }
                        val sat = cObj.optDouble("sat", 0.0).toFloat()
                        val value = cObj.optDouble("val", 0.0).toFloat()
                        val edge = cObj.optDouble("edge", 0.0).toFloat()
                        cellsList.add(SpatialCell(hues, sat, value, edge))
                    }
                }

                val domArray = obj.optJSONArray("domHues")
                val domBins = mutableListOf<Int>()
                if (domArray != null) {
                    for (d in 0 until domArray.length()) {
                        domBins.add(domArray.getInt(d))
                    }
                }

                val lumArray = obj.optJSONArray("lum")
                val lum = FloatArray(64)
                if (lumArray != null) {
                    for (l in 0 until min(64, lumArray.length())) {
                        lum[l] = lumArray.getDouble(l).toFloat()
                    }
                }

                list.add(
                    PrayerMatSignature(
                        id = id,
                        cells = cellsList,
                        dominantHueBins = domBins.toIntArray(),
                        luminanceMatrix = lum
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
