package off.kys.backtalk.util

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Utility class for playing audio files.
 */
class AudioPlayer {

    private var player: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath = _currentPath.asStateFlow()

    fun playFile(file: File, onCompletion: () -> Unit = {}) {
        stop()
        _currentPath.value = file.absolutePath
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            _isPlaying.value = true
            startProgressUpdate()
            
            setOnCompletionListener {
                _isPlaying.value = false
                _progress.value = 1f
                stopProgressUpdate()
                onCompletion()
            }
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                player?.let {
                    if (it.isPlaying && it.duration > 0) {
                        _progress.value = it.currentPosition.toFloat() / it.duration
                    }
                }
                delay(16) // ~60fps for smooth progress
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun stop() {
        stopProgressUpdate()
        player?.stop()
        player?.release()
        player = null
        _isPlaying.value = false
        _progress.value = 0f
        _currentPath.value = null
    }
    
    fun pause() {
        player?.pause()
        _isPlaying.value = false
        stopProgressUpdate()
    }
    
    fun resume() {
        player?.start()
        _isPlaying.value = true
        startProgressUpdate()
    }
}
