package com.example.mynotes.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerManager @Inject constructor(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying: Boolean = false
        private set

    fun playAudio(filePath: String, onComplete: () -> Unit = {}) {
        stopAudio()
        val file = File(filePath)
        if (!file.exists()) return

        try {
            val player = MediaPlayer.create(context, Uri.fromFile(file)) ?: return
            player.setOnCompletionListener {
                isPlaying = false
                stopAudio()
                onComplete()
            }
            player.start()
            mediaPlayer = player
            isPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isPlaying = false
    }
}
