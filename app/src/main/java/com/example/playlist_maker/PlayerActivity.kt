package com.example.playlist_maker

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.example.playlist_maker.databinding.ActivityPlayerBinding
import java.text.SimpleDateFormat
import java.util.*


class PlayerActivity : AppCompatActivity() {

    private lateinit var playerBinding: ActivityPlayerBinding
    private var mediaPlayer = MediaPlayer()
    private var playerState = PlayerState.STATE_DEFAULT
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val PLAYTIME_DELAY = 250L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerBinding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(playerBinding.root)

        playerBinding.toolbarInclude.toolbar.apply {
            title = ""
            setSupportActionBar(this)
            setNavigationOnClickListener {
                finish()
            }
        }

        val track = Gson().fromJson(intent.getStringExtra(TRACK), Track::class.java)

        Glide
            .with(playerBinding.mediaTrackImage)
            .load(track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius_8)))
            .into(playerBinding.mediaTrackImage)

        playerBinding.trackName.text = track.trackName
        playerBinding.artistName.text = track.artistName
        playerBinding.primaryGenreName.text = track.primaryGenreName
        playerBinding.country.text = track.country

        playerBinding.trackTime.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(track.releaseDate)
        if (date != null) {
            val formattedDatesString = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
            playerBinding.releaseDate.text = formattedDatesString
        }

        if (track.collectionName.isNotEmpty()) {
            playerBinding.collectionName.text = track.collectionName
        } else {
            playerBinding.collectionName.visibility = View.GONE
            playerBinding.trackAlbum.visibility = View.GONE
        }

        track?.previewUrl?.let { preparePlayer(it) }

        playerBinding.playButton.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(updateTimer())
    }

    private fun preparePlayer(previewUrl: String) {
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerBinding.playButton.isEnabled = true
            playerState = PlayerState.STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            playerState = PlayerState.STATE_PREPARED
            handler.removeCallbacks(updateTimer())
            playerBinding.playButton.setImageResource(R.drawable.ic_play)
            playerBinding.time.text = getString(R.string.track_time)
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerBinding.playButton.setImageResource(R.drawable.ic_pause)
        playerState = PlayerState.STATE_PLAYING
    }

    private fun pausePlayer() {
        handler.removeCallbacks(updateTimer())
        mediaPlayer.pause()
        playerBinding.playButton.setImageResource(R.drawable.ic_play)
        playerState = PlayerState.STATE_PAUSED
    }

    private fun playbackControl() {
        when (playerState) {
            PlayerState.STATE_PLAYING -> {
                pausePlayer()
            }

            PlayerState.STATE_PREPARED, PlayerState.STATE_PAUSED -> {
                startPlayer()
                startTimer()
            }

            else -> {}
        }
    }

    private fun startTimer() {
        handler.post(
            updateTimer()
        )
    }

    private fun updateTimer(): Runnable {
        return object : Runnable {
            override fun run() {
                if (playerState == PlayerState.STATE_PLAYING) {
                    playerBinding.time.text = SimpleDateFormat(
                        "mm:ss",
                        Locale.getDefault()
                    ).format(mediaPlayer.currentPosition)
                    handler.postDelayed(this, PLAYTIME_DELAY)
                }
            }
        }
    }
}