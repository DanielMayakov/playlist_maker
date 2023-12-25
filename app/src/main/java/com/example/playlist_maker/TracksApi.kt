package com.example.playlist_maker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TracksApi {

    @GET("/search?entity=song")
    fun searchTrack(@Query("term") text: String): Call<TrackResponse>
}