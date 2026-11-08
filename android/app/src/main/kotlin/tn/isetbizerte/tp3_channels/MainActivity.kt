
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
    private val REQUEST_CODE = 1001

    private var pendingPermissionResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

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

        Vibreur(this).enregistrer(flutterEngine.dartExecutor.binaryMessenger)

        CapteurLumiere(this).enregistrer(flutterEngine.dartExecutor.binaryMessenger)

        NiveauSonore(this).enregistrer(flutterEngine.dartExecutor.binaryMessenger)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            AUDIO_PERMISSION
        ).setMethodCallHandler { call, result ->

            if (call.method == "demander") {

                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    result.success(true)
                } else {
                    pendingPermissionResult = result

                    requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_CODE
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
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED

            pendingPermissionResult?.success(granted)
            pendingPermissionResult = null
        }
    }
}
