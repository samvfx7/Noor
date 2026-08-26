package com.samvfx7.noor.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class QiblaCompassState(
    val deviceAzimuthDeg: Float = 0f,
    val qiblaBearingDeg: Float = 0f,
    val angleDifferenceDeg: Float = 0f,
    val isAlignedWithKaaba: Boolean = false,
    val isSensorAvailable: Boolean = true,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
)

class QiblaSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassState = MutableStateFlow(QiblaCompassState())
    val compassState: StateFlow<QiblaCompassState> = _compassState.asStateFlow()

    private var targetQiblaBearing: Float = 0f
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private var smoothedAzimuth: Float = 0f

    fun start(targetBearing: Float) {
        this.targetQiblaBearing = targetBearing

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            _compassState.value = _compassState.value.copy(isSensorAvailable = true, qiblaBearingDeg = targetBearing)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            _compassState.value = _compassState.value.copy(isSensorAvailable = true, qiblaBearingDeg = targetBearing)
        } else {
            _compassState.value = _compassState.value.copy(isSensorAvailable = false, qiblaBearingDeg = targetBearing)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var azimuth = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            azimuth = (azimuth + 360f) % 360f
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic && event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuth = (azimuth + 360f) % 360f
            }
        }

        // Low-pass filter for buttery smooth compass animation
        val alpha = 0.15f
        var diff = azimuth - smoothedAzimuth
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        smoothedAzimuth = (smoothedAzimuth + alpha * diff + 360f) % 360f

        var angleDiff = targetQiblaBearing - smoothedAzimuth
        while (angleDiff < -180f) angleDiff += 360f
        while (angleDiff > 180f) angleDiff -= 360f

        val isAligned = kotlin.math.abs(angleDiff) <= 3.0f

        _compassState.value = _compassState.value.copy(
            deviceAzimuthDeg = smoothedAzimuth,
            qiblaBearingDeg = targetQiblaBearing,
            angleDifferenceDeg = angleDiff,
            isAlignedWithKaaba = isAligned,
            accuracy = event.accuracy
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _compassState.value = _compassState.value.copy(accuracy = accuracy)
    }
}
