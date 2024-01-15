package com.example.playlist_maker

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.playlist_maker.SearchActivity.Companion.TRACKS_HISTORY_KEY
import com.example.playlist_maker.SearchActivity.Companion.TRACKS_HISTORY_SIZE

class SearchHistory(private val sharedPrefs: SharedPreferences) {

    fun addTrack(track: Track) {
        val tracksHistory = getTracks().toMutableList()
        tracksHistory.remove(track)
        tracksHistory.add(0, track)
        if (tracksHistory.size > TRACKS_HISTORY_SIZE) tracksHistory.removeLast()
        saveTracks(tracksHistory)
    }

    fun getTracks(): List<Track> {
        val json = sharedPrefs.getString(TRACKS_HISTORY_KEY, null) ?: return emptyList()
        return Gson().fromJson(json, object : TypeToken<ArrayList<Track>>() {}.type)
    }

    fun clear() {
        sharedPrefs.edit { remove(TRACKS_HISTORY_KEY) }
    }

    private fun saveTracks(tracks: List<Track>) {
        val json = Gson().toJson(tracks)
        sharedPrefs.edit().putString(TRACKS_HISTORY_KEY, json).apply()
    }
}