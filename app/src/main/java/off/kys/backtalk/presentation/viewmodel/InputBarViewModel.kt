package off.kys.backtalk.presentation.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.event.InputBarEvent
import off.kys.backtalk.presentation.state.InputBarEffect
import off.kys.backtalk.presentation.state.InputBarUiState
import off.kys.backtalk.presentation.status.SchedulingStage
import off.kys.backtalk.util.AudioRecorder
import off.kys.backtalk.util.HashUtils
import off.kys.backtalk.util.MediaUtils
import java.io.File

class InputBarViewModel(
    private val application: Application,
    private val preferences: BacktalkPreferences,
    private val onMessageSend: (String) -> Unit,
    private val onVoiceSend: (String, Long, List<Float>) -> Unit,
    private val onMessageSchedule: (String, Long) -> Unit,
    private val onSharedImageSendAction: (List<String>, String) -> Unit,
    private val onAttachClickAction: () -> Unit,
    private val onCancelReplyAction: () -> Unit,
    private val onCancelEditAction: () -> Unit
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        InputBarUiState(linkPreviewEnabled = preferences.linkPreviewEnabled)
    )
    val uiState: StateFlow<InputBarUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<InputBarEffect>()
    val effect: SharedFlow<InputBarEffect> = _effect.asSharedFlow()

    private val audioRecorder = AudioRecorder(application)
    private var recordingStartTime = 0L

    init {
        observeAmplitudes()
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun onEvent(event: InputBarEvent) = when (event) {
        is InputBarEvent.SendMessage -> handleSendMessage(event.text)
        is InputBarEvent.ScheduleMessage -> handleScheduleMessage(event.text, event.timestamp)
        is InputBarEvent.UpdateReplyingTo -> _uiState.update { it.copy(replyingTo = event.message) }
        is InputBarEvent.UpdateEditingMessage -> _uiState.update { it.copy(editingMessage = event.message) }
        InputBarEvent.CancelReply -> {
            _uiState.update { it.copy(replyingTo = null) }
            onCancelReplyAction()
        }
        InputBarEvent.CancelEdit -> {
            _uiState.update { it.copy(editingMessage = null) }
            onCancelEditAction()
        }
        InputBarEvent.AttachClicked -> onAttachClickAction()
        InputBarEvent.StartRecording -> startVoiceRecording()
        InputBarEvent.CancelRecording -> cancelVoiceRecording()
        InputBarEvent.StopAndSendRecording -> stopAndSendVoiceRecording()
        InputBarEvent.ShowTapHint -> handleShowTapHint()
        InputBarEvent.ClearTapHint -> _uiState.update { it.copy(showTapHint = false) }
        is InputBarEvent.UpdateOffsetX -> _uiState.update { it.copy(offsetX = event.x) }
        is InputBarEvent.ChangeSchedulingStage -> _uiState.update { it.copy(schedulingStage = event.stage) }
        InputBarEvent.RequestExactAlarmPermission -> checkAndRequestExactAlarmPermission()
        InputBarEvent.DismissPermissionRationale -> _uiState.update { it.copy(showPermissionRationale = false) }
        InputBarEvent.CancelSharedImage -> _uiState.update { it.copy(sharedImageUris = emptyList()) }
        is InputBarEvent.SendSharedImages -> handleSendSharedImages(event.uris, event.caption)
        is InputBarEvent.ContentReceived -> handleContentReceived(event.transferableContent)
    }

    private fun observeAmplitudes() {
        viewModelScope.launch {
            audioRecorder.amplitudes.collect { list ->
                _uiState.update { it.copy(amplitudes = list) }
            }
        }
    }

    private fun handleSendMessage(text: String) {
        if (text.isNotBlank()) {
            onMessageSend(text)
            _uiState.value.textFieldState.clearText()
        }
    }

    private fun handleScheduleMessage(text: String, timestamp: Long) {
        onMessageSchedule(text, timestamp)
        _uiState.value.textFieldState.clearText()
        _uiState.update { it.copy(schedulingStage = SchedulingStage.Hidden) }
    }

    private fun startVoiceRecording() {
        _uiState.update { it.copy(isRecording = true, secondsElapsed = 0) }
        recordingStartTime = System.currentTimeMillis()
        audioRecorder.startRecording()
        
        viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(1000L)
                _uiState.update { it.copy(secondsElapsed = it.secondsElapsed + 1) }
            }
        }
    }

    private fun cancelVoiceRecording() {
        _uiState.update { it.copy(isRecording = false) }
        audioRecorder.cancelRecording()
    }

    private fun stopAndSendVoiceRecording() {
        _uiState.update { it.copy(isRecording = false) }
        val file = audioRecorder.stopRecording()
        if (file != null) {
            val duration = System.currentTimeMillis() - recordingStartTime
            onVoiceSend(file.absolutePath, duration, _uiState.value.amplitudes)
        }
    }

    private fun handleShowTapHint() {
        _uiState.update { it.copy(showTapHint = true) }
        viewModelScope.launch {
            _effect.emit(InputBarEffect.TriggerShake)
            delay(2000L)
            _uiState.update { it.copy(showTapHint = false) }
        }
    }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = application.getSystemService(AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                _uiState.update { it.copy(showPermissionRationale = true) }
                return
            }
        }
        _uiState.update { it.copy(schedulingStage = SchedulingStage.SelectingDate) }
    }

    private fun handleSendSharedImages(uris: List<String>, caption: String) {
        onSharedImageSendAction(uris, caption)
        _uiState.update { it.copy(sharedImageUris = emptyList()) }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun handleContentReceived(transferableContent: TransferableContent) {
        if (!transferableContent.hasMediaType(MediaType.Image)) return

        val clipData = transferableContent.clipEntry.clipData
        viewModelScope.launch(Dispatchers.IO) {
            val processedUris = mutableListOf<String>()
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri ?: continue
                val mimeType = application.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
                val mediaDir = File(application.filesDir, "media").apply { mkdirs() }

                val smartPointing = preferences.smartImagePointingEnabled
                val fileName = if (smartPointing) {
                    val hash = application.contentResolver.openInputStream(uri)?.use { HashUtils.calculateSha256(it) }
                        ?: System.currentTimeMillis().toString()
                    "media_$hash.$extension"
                } else {
                    "media_${System.currentTimeMillis()}_${uri.lastPathSegment}.$extension"
                }

                val destFile = File(mediaDir, fileName)
                if (!(smartPointing && destFile.exists())) {
                    application.contentResolver.openInputStream(uri)?.use { input ->
                        destFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (preferences.removeImageMetadataEnabled && mimeType.startsWith("image/") && mimeType != "image/gif") {
                        MediaUtils.stripImageMetadata(destFile)
                    }
                }
                processedUris.add(Uri.fromFile(destFile).toString())
            }

            if (processedUris.isNotEmpty()) {
                handleSendSharedImages(processedUris, "")
            }
        }
    }
}