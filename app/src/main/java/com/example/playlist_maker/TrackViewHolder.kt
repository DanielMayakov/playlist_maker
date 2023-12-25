package com.example.playlist_maker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker.Track
import java.text.SimpleDateFormat
import java.util.*

class TrackViewHolder (itemView: View): RecyclerView.ViewHolder (itemView) {

    private val artistName = itemView.findViewById<TextView>(R.id.artistName)
    private val trackName = itemView.findViewById<TextView>(R.id.trackName)
    private val trackTime = itemView.findViewById<TextView>(R.id.trackTime)
    private val trackImage = itemView.findViewById<ImageView>(R.id.trackImage)

    fun bind (model: Track) {
        artistName.text = model.artistName
        trackName.text = model.trackName
        trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(model.trackTimeMillis)

        Glide.with(itemView.context)
            .load(model.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.corner_radius)))
            .into(trackImage)
    }

}