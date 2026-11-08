
package tn.isetbizerte.tp3_channels

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

class Vibreur(private val context: Context) {

    fun enregistrer(messenger: BinaryMessenger) {
        MethodChannel(messenger, "appareil/vibrer")
            .setMethodCallHandler { call, result ->

                if (call.method == "vibrer") {
                    val motif = call.argument<String>("motif")

                    val vibrator = context.getSystemService(
                        Context.VIBRATOR_SERVICE
                    ) as Vibrator

                    if (!vibrator.hasVibrator()) {
                        result.error(
                            "PAS_DE_VIBREUR",
                            "Cet appareil ne possède pas de vibreur",
                            null
                        )
                        return@setMethodCallHandler
                    }

                    val effect = when (motif) {
                        "court" -> VibrationEffect.createOneShot(
                            200,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )

                        "long" -> VibrationEffect.createOneShot(
                            600,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )

                        "sos" -> VibrationEffect.createWaveform(
                            longArrayOf(
                                0, 100, 100, 100, 100, 100,
                                300, 300, 100, 300, 100, 300,
                                100, 100, 100, 100, 100, 100
                            ),
                            -1
                        )

                        else -> {
                            result.error(
                                "MOTIF_INCONNU",
                                "Motif de vibration inconnu",
                                motif
                            )
                            return@setMethodCallHandler
                        }
                    }

                    vibrator.vibrate(effect)
                    result.success("Vibration déclenchée")
                } else {
                    result.notImplemented()
                }
            }
    }
}
