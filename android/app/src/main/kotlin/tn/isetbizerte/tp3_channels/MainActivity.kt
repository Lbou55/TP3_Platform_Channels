
package tn.isetbizerte.tp3_channels

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "appareil/infos"
    private val AUDIO_PERMISSION = "audio/permission"
    private val CAMERA_PERMISSION = "camera/permission"

    private val AUDIO_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE = 1002

    private var pendingAudioPermissionResult: MethodChannel.Result? = null
    private var pendingCameraPermissionResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val messenger = flutterEngine.dartExecutor.binaryMessenger

        MethodChannel(messenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "lire") {
                val infos = mapOf(
                    "fabricant" to Build.MANUFACTURER,
                    "modele" to Build.MODEL,
                    "version" to Build.VERSION.RELEASE,
                    "sdk" to Build.VERSION.SDK_INT
                )
                result.success(infos)
            } else {
                result.notImplemented()
            }
        }

        Vibreur(this).enregistrer(messenger)
        CapteurLumiere(this).enregistrer(messenger)
        NiveauSonore(this).enregistrer(messenger)
        CapteurProximite(this).enregistrer(messenger)
        Torche(this).enregistrer(messenger)

        MethodChannel(messenger, AUDIO_PERMISSION).setMethodCallHandler { call, result ->
            if (call.method == "demander") {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    result.success(true)
                } else {
                    pendingAudioPermissionResult = result
                    requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        AUDIO_REQUEST_CODE
                    )
                }
            } else {
                result.notImplemented()
            }
        }

        MethodChannel(messenger, CAMERA_PERMISSION).setMethodCallHandler { call, result ->
            if (call.method == "demander") {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    result.success(true)
                } else {
                    pendingCameraPermissionResult = result
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE
                    )
                }
            } else {
                result.notImplemented()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val granted = grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED

        when (requestCode) {
            AUDIO_REQUEST_CODE -> {
                pendingAudioPermissionResult?.success(granted)
                pendingAudioPermissionResult = null
            }

            CAMERA_REQUEST_CODE -> {
                pendingCameraPermissionResult?.success(granted)
                pendingCameraPermissionResult = null
            }
        }
    }
}
