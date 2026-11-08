
package tn.isetbizerte.tp3_channels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.SystemClock
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.BinaryMessenger
import kotlinx.coroutines.*

class NiveauSonore(private val context: Context) : EventChannel.StreamHandler {

    private var sink: EventChannel.EventSink? = null
    private var audioRecord: AudioRecord? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val sampleRate = 44100

    fun enregistrer(messenger: BinaryMessenger) {
        EventChannel(messenger, "audio/niveau").setStreamHandler(this)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        sink = events

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            sink?.error("PERMISSION_REFUSEE", "Permission microphone refusée", null)
            return
        }

        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (bufferSize <= 0) {
            sink?.error("AUDIO_INDISPONIBLE", "Buffer audio invalide", null)
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val recorder = audioRecord ?: return

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                recorder.release()
                audioRecord = null
                sink?.error("AUDIO_INDISPONIBLE", "Initialisation audio échouée", null)
                return
            }

            recorder.startRecording()

            job = scope.launch(Dispatchers.IO) {
                var dernierEnvoi = 0L
                val buffer = ShortArray(bufferSize / 2)

                try {
                    while (isActive) {
                        val count = recorder.read(buffer, 0, buffer.size)

                        if (count > 0) {
                            var sum = 0.0

                            for (i in 0 until count) {
                                val value = buffer[i].toDouble()
                                sum += value * value
                            }

                            val rms = kotlin.math.sqrt(sum / count)
                            val db = if (rms > 0) {
                                20 * kotlin.math.log10(rms / 32768.0)
                            } else {
                                -90.0
                            }

                            val maintenant = SystemClock.elapsedRealtime()

if (maintenant - dernierEnvoi >= 200) {
    dernierEnvoi = maintenant

    withContext(Dispatchers.Main) {
        sink?.success(db)
    }
}
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        sink?.error("ERREUR_AUDIO", e.message, null)
                    }
                }
            }

        } catch (e: SecurityException) {
            sink?.error("PERMISSION_REFUSEE", e.message, null)
        } catch (e: Exception) {
            sink?.error("ERREUR_AUDIO", e.message, null)
        }
    }

    override fun onCancel(arguments: Any?) {
        job?.cancel()
        job = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {
        }

        audioRecord = null
        sink = null
    }

    fun fermer() {
        onCancel(null)
        scope.cancel()
    }
}
