package com.example.playlist_maker

import androidx.recyclerview.widget.DiffUtil

class TracksDiffCallback(
    private val oldList: List<Track>,
    private val newList: List<Track>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTrack = oldList[oldItemPosition]
        val newTrack = newList[oldItemPosition]
        return oldTrack.trackId == newTrack.trackId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTrack = oldList[oldItemPosition]
        val newTrack = newList[oldItemPosition]
        return oldTrack == newTrack
    }
}