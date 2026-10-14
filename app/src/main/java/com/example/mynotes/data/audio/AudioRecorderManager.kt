package com.example.mynotes.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorderManager @Inject constructor(
    private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMs: Long = 0

    fun startRecording(): File? {
        val audioDir = File(context.filesDir, "voice_memos").apply {
            if (!exists()) mkdirs()
        }
        val outputFile = File(audioDir, "memo_${System.currentTimeMillis()}.m4a")

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(FileOutputStream(outputFile).fd)
                prepare()
                start()
            }

            mediaRecorder = recorder
            currentOutputFile = outputFile
            startTimeMs = System.currentTimeMillis()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun stopRecording(): RecordResult? {
        val recorder = mediaRecorder ?: return null
        val durationSec = ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()

        return try {
            recorder.stop()
            recorder.release()
            mediaRecorder = null
            val file = currentOutputFile
            if (file != null && file.exists()) {
                RecordResult(file.absolutePath, durationSec)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            mediaRecorder = null
            null
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        currentOutputFile?.delete()
        currentOutputFile = null
    }

    data class RecordResult(
        val filePath: String,
        val durationSeconds: Int
    )
}
