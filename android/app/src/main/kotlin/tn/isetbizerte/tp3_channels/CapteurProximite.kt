
package tn.isetbizerte.tp3_channels

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel

class CapteurProximite(context: Context) :
    EventChannel.StreamHandler, SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var eventSink: EventChannel.EventSink? = null

    fun enregistrer(messenger: BinaryMessenger) {
        EventChannel(messenger, "capteur/proximite")
            .setStreamHandler(this)
    }

    override fun onListen(
        arguments: Any?,
        events: EventChannel.EventSink
    ) {
        eventSink = events

        if (sensor == null) {
            events.error(
                "CAPTEUR_ABSENT",
                "Capteur de proximité non disponible",
                null
            )
            return
        }

        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val proche = event.values[0] < event.sensor.maximumRange
        eventSink?.success(proche)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCancel(arguments: Any?) {
        sensorManager.unregisterListener(this)
        eventSink = null
    }
}
