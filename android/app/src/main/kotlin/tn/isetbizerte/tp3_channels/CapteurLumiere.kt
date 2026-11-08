
package tn.isetbizerte.tp3_channels

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.flutter.plugin.common.EventChannel

class CapteurLumiere(private val context: Context) : EventChannel.StreamHandler {

    private val manager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val capteur = manager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var sink: EventChannel.EventSink? = null

    private val listener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            sink?.success(event.values[0].toDouble())
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun enregistrer(messenger: io.flutter.plugin.common.BinaryMessenger) {
        EventChannel(messenger, "capteur/lumiere")
            .setStreamHandler(this)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        sink = events

        if (capteur == null) {
            sink?.error(
                "CAPTEUR_ABSENT",
                "Ce téléphone ne possède pas de capteur de lumière",
                null
            )
            return
        }

        manager.registerListener(
            listener,
            capteur,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onCancel(arguments: Any?) {
        manager.unregisterListener(listener)
        sink = null
    }
}
