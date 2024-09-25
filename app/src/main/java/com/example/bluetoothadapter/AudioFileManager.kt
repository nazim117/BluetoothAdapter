package com.example.bluetoothadapter

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

class AudioFileManager(private val context: Context) {
    private lateinit var mediaPlayer: MediaPlayer
    val handler = Handler(Looper.getMainLooper())
    var isPlaying = false

    private val TAG = "AudioManager"

    fun startAudioPlayback() {
        if(!::mediaPlayer.isInitialized){
            mediaPlayer = MediaPlayer.create(context, R.raw.audio_file)
        }

        handler.postDelayed(object: Runnable{
            override fun run(){
                playAudio()
                handler.postDelayed(this, 30000)
            }
        }, 0)

        isPlaying = true
    }

    private fun playAudio() {
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.prepare()
        }
        mediaPlayer.start()
        Log.d(TAG, "Audio playback started")
    }

    fun stopAudioPlayback() {
        handler.removeCallbacksAndMessages(null)
        if(::mediaPlayer.isInitialized && mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.prepare()
        }
        isPlaying = false
    }

    fun onDestroy(){
        handler.removeCallbacksAndMessages(null)
        if(::mediaPlayer.isInitialized){
            if(mediaPlayer.isPlaying){
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }
}