package com.example.playlist_maker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }


}