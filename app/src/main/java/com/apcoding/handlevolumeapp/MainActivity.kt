package com.apcoding.handlevolumeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var audioManager : AudioManager

    private lateinit var mediaPlayerView : PlayerView
    private lateinit var ivMute: ImageView
    private var player: ExoPlayer? = null

    private val volumeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                updateMuteIcon()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayerView = findViewById(R.id.mediaPlayerView)
        ivMute = findViewById(R.id.ivMute)

        // Initialize the player
        player = ExoPlayer.Builder(this).build()
        mediaPlayerView.player = player

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Set data source (URL) for the media player
        val videoUrl = "https://www.w3schools.com/tags/mov_bbb.mp4"
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))
            .setMimeType("video/mp4")
            .build()

        // Set media item to the MediaPlayerView
        player?.setMediaItem(mediaItem)

        // Start preparing the MediaPlayerView
        player?.prepare()
        player?.playWhenReady = true

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)

                Log.e("MainActivity", "Player error: ${error.message}")
            }
        })

        // Set click listener for mute button
        ivMute.setOnClickListener {
            toggleMute()
        }

        // Set initial state of mute button
        updateMuteIcon()
    }

    private fun toggleMute() {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (currentVolume == 0) {
            // If volume is muted, un mute
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
        } else {
            // If volume is not muted, mute
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        }
        // Update mute icon
        updateMuteIcon()
    }

    private fun updateMuteIcon() {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (currentVolume == 0) {
            // Volume is muted
            ivMute.setImageResource(R.drawable.icn_volume_off)
        } else {
            // Volume is not muted
            ivMute.setImageResource(R.drawable.icn_volume_on)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        unregisterReceiver(volumeChangeReceiver)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeChangeReceiver, filter)
    }
}