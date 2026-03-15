package com.wakee.app.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Timer
import java.util.TimerTask

class AudioRecordingService(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var outputFile: File? = null
    private var timer: Timer? = null
    private var recordingDuration = 0

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _recordedUri = MutableStateFlow<Uri?>(null)
    val recordedUri: StateFlow<Uri?> = _recordedUri.asStateFlow()

    private val maxDuration = 15 // seconds, matches iOS

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "alarm_recording_${System.currentTimeMillis()}.m4a")
            outputFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setMaxDuration(maxDuration * 1000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            _isRecording.value = true
            recordingDuration = 0
            _recordingSeconds.value = 0

            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    recordingDuration++
                    _recordingSeconds.value = recordingDuration
                    if (recordingDuration >= maxDuration) {
                        stopRecording()
                    }
                }
            }, 1000, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
        }
    }

    fun stopRecording() {
        try {
            timer?.cancel()
            timer = null
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            outputFile?.let {
                if (it.exists()) {
                    _recordedUri.value = it.toUri()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
        }
    }

    fun startPlayback() {
        val uri = _recordedUri.value ?: return
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                }
                start()
            }
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPlayback() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun deleteRecording() {
        stopPlayback()
        outputFile?.delete()
        outputFile = null
        _recordedUri.value = null
        _recordingSeconds.value = 0
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }
}
