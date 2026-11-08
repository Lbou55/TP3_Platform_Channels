
package tn.isetbizerte.tp3_channels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

class Torche(context: Context) {

    private val appContext = context.applicationContext

    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun enregistrer(messenger: BinaryMessenger) {
        MethodChannel(messenger, "appareil/torche")
            .setMethodCallHandler { call, result ->

                if (call.method == "allumer") {

                    if (ContextCompat.checkSelfPermission(
                            appContext,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        result.error(
                            "PERMISSION_REFUSEE",
                            "Permission caméra nécessaire",
                            null
                        )
                        return@setMethodCallHandler
                    }

                    try {
                        val etat = call.argument<Boolean>("etat") ?: false

                        val cameraId = cameraManager.cameraIdList.firstOrNull {
                            cameraManager.getCameraCharacteristics(it)
                                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                        }

                        if (cameraId == null) {
                            result.error(
                                "PAS_DE_FLASH",
                                "Aucun flash disponible",
                                null
                            )
                        } else {
                            cameraManager.setTorchMode(cameraId, etat)

                            result.success(
                                if (etat) "Torche allumée" else "Torche éteinte"
                            )
                        }

                    } catch (e: Exception) {
                        result.error(
                            "ERREUR_TORCHE",
                            e.message ?: "Erreur inconnue",
                            null
                        )
                    }

                } else {
                    result.notImplemented()
                }
            }
    }
}
