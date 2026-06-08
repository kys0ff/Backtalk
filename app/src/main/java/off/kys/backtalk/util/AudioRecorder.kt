package off.kys.backtalk.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for recording audio and providing real-time amplitude data.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null
    
    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    val amplitudes = _amplitudes.asStateFlow()

    private var amplitudeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startRecording() {
        val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
        recordingFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(file).fd)

            prepare()
            start()
        }

        _amplitudes.value = emptyList()
        amplitudeJob = scope.launch {
            while (true) {
                delay(100)
                val amplitude = try {
                    recorder?.maxAmplitude ?: 0
                } catch (_: Exception) {
                    0
                }
                val normalizedAmplitude = (amplitude.toFloat() / 32767f).coerceIn(0f, 1f)
                _amplitudes.value += normalizedAmplitude
            }
        }
    }

    fun stopRecording(): File? {
        amplitudeJob?.cancel()
        amplitudeJob = null
        
        try {
            recorder?.stop()
        } catch (_: Exception) {
            recordingFile?.delete()
            recordingFile = null
        }
        recorder?.release()
        recorder = null
        
        val result = recordingFile
        recordingFile = null
        return result
    }

    fun cancelRecording() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        
        try {
            recorder?.stop()
        } catch (_: Exception) {
            // Ignore
        }
        recorder?.release()
        recorder = null
        
        recordingFile?.delete()
        recordingFile = null
        _amplitudes.value = emptyList()
    }
}
