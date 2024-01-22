package com.example.playlist_maker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.gson.Gson
import com.example.playlist_maker.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    lateinit var searchBinding: ActivitySearchBinding
    private val tracksBaseUrl = "https://itunes.apple.com"
    private var searchInputQuery = ""
    private val searchHistory by lazy {
        val sharedPrefs = getSharedPreferences(App.PLAYLIST_MAKER_SHARED_PREFS, MODE_PRIVATE)
        SearchHistory(sharedPrefs)
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(tracksBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val tracksApi = retrofit.create(TracksApi::class.java)

    private val trackAdapter = TrackAdapter {
        showPlayer(it)
    }

    private val historyAdapter = TrackAdapter {
        showPlayer(it)
    }

    private fun showPlayer(track: Track) {
        searchHistory.addTrack(track)
        val displayIntent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(TRACK, Gson().toJson(track))
        }
        startActivity(displayIntent)
    }

    companion object {
        const val SEARCH_QUERY = "search_query"
        const val TRACKS_HISTORY_KEY = "tracks_history_key"
        const val TRACKS_HISTORY_SIZE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(searchBinding.root)

        searchBinding.inputEditText.requestFocus()
        searchBinding.settingsToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        searchBinding.clearImageView.setOnClickListener { clearSearchForm() }
        searchBinding.inputEditText.doOnTextChanged { text, _, _, _ ->
            searchInputQuery = text.toString()
            searchBinding.clearImageView.isVisible = searchInputQuery.isNotEmpty()
            if (searchBinding.inputEditText.hasFocus() && searchInputQuery.isNotEmpty()) {
                showState(State.SEARCH_RESULT)
            }
        }

        searchBinding.searchRecycler.adapter = trackAdapter
        searchBinding.searchHistoryRecycler.adapter = historyAdapter

        searchBinding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchBinding.inputEditText.text.isEmpty()) {
                showState(State.SEARCH_RESULT)
            }
        }

        searchBinding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchTrack()
            }
            false
        }
        refresh()

        if (searchBinding.inputEditText.text.isEmpty()) {
            historyAdapter.tracks = searchHistory.getTracks().toMutableList()
            if (historyAdapter.tracks.isNotEmpty()) {
                showState(State.TRACKS_HISTORY)
            }
        }

        searchBinding.clearHistoryButton.setOnClickListener {
            searchHistory.clear()
            showState(State.SEARCH_RESULT)
        }
    }

    private fun refresh() {
        searchBinding.refreshButton.setOnClickListener {
            searchTrack()
        }
    }

    private fun searchTrack() {
        tracksApi.searchTrack(searchBinding.inputEditText.text.toString())
            .enqueue(object : Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            if (response.body()?.results?.isNotEmpty() == true) {
                                trackAdapter.tracks =
                                    response.body()?.results!! as MutableList<Track>
                                showState(State.SEARCH_RESULT)
                            } else {
                                showState(State.NOTHING_FOUND)
                            }
                        }

                        else -> {
                            showState(State.INTERNET_PROBLEM)
                        }
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    showState(State.INTERNET_PROBLEM)
                }
            })
    }

    private fun showState(state: State) {
        with(searchBinding) {
            searchRecycler.visibility = if (state == State.SEARCH_RESULT) VISIBLE else GONE
            nothingFound.visibility = if (state == State.NOTHING_FOUND) VISIBLE else GONE
            internetProblem.visibility = if (state == State.INTERNET_PROBLEM) VISIBLE else GONE
            searchHistoryLayout.visibility = if (state == State.TRACKS_HISTORY) VISIBLE else GONE
        }
    }

    private fun clearSearchForm() {
        searchBinding.inputEditText.text.clear()
        historyAdapter.tracks = searchHistory.getTracks().toMutableList()
        if (historyAdapter.tracks.isNotEmpty()) {
            showState(State.TRACKS_HISTORY)
        } else {
            showState(State.SEARCH_RESULT)
        }
        clearPlaceholders()
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun clearPlaceholders() {
        searchBinding.nothingFound.visibility = GONE
        searchBinding.internetProblem.visibility = GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY, searchInputQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchInputQuery = savedInstanceState.getString(SEARCH_QUERY, "")
        if (searchInputQuery.isNotEmpty()) {
            searchBinding.inputEditText.setText(searchInputQuery)
            searchTrack()
        }
    }
}