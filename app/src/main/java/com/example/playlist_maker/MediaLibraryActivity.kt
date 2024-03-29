package com.example.playlist_maker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playlist_maker.databinding.ActivityMediaLibraryBinding

class MediaLibraryActivity : AppCompatActivity() {
    private var mediaBinding: ActivityMediaLibraryBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBinding = ActivityMediaLibraryBinding.inflate(layoutInflater)
        setContentView(mediaBinding?.root)
    }
}