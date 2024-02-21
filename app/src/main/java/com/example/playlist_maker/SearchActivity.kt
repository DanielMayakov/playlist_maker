package com.example.playlist_maker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
    private val tracksBaseUrl = "http://itunes.apple.com"
    private var searchInputQuery = ""
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchTrack() }

    private val retrofit = Retrofit.Builder()
        .baseUrl(tracksBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val tracksApi = retrofit.create(TracksApi::class.java)

    private val trackAdapter = TrackAdapter {
        if (clickDebounce()) {
            showPlayer(it)
        }
    }

    private val historyAdapter = TrackAdapter {
        if (clickDebounce()) {
            showPlayer(it)
        }
    }

    private fun showPlayer(track: Track) {
        searchHistory.addTrack(track)
        val displayIntent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(TRACK, Gson().toJson(track))
        }
        startActivity(displayIntent)
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    companion object {
        const val SEARCH_QUERY = "search_query"
        const val TRACKS_HISTORY_KEY = "tracks_history_key"
        const val TRACKS_HISTORY_SIZE = 10
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private val simpleTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, s1: Int, s2: Int, s3: Int) {
            searchInputQuery = s.toString()
            searchBinding.clearImageView.isVisible = !s.isNullOrEmpty()

            if (searchBinding.inputEditText.hasFocus() && searchInputQuery.isNotEmpty()) {
                searchDebounce()
                showState(State.SEARCH_RESULT)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    }

    private val searchHistory by lazy {
        val sharedPrefs = getSharedPreferences(App.PLAYLIST_MAKER_SHARED_PREFS, MODE_PRIVATE)
        SearchHistory(sharedPrefs)
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
        searchBinding.progressBar.visibility = VISIBLE
        searchBinding.searchRecycler.visibility = GONE
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

    fun showState(state: State) {
        searchBinding.searchRecycler.visibility = when (state) {
            State.NOTHING_FOUND -> View.GONE
            State.INTERNET_PROBLEM -> View.GONE
            State.SEARCH_RESULT -> View.VISIBLE
            else -> View.GONE
        }

        searchBinding.nothingFound.visibility =
            if (state == State.NOTHING_FOUND) View.VISIBLE else View.GONE
        searchBinding.internetProblem.visibility =
            if (state == State.INTERNET_PROBLEM) View.VISIBLE else View.GONE
        searchBinding.searchHistoryLayout.visibility =
            if (state != State.SEARCH_RESULT) View.GONE else View.VISIBLE
        searchBinding.progressBar.visibility =
            if (state == State.SEARCH_RESULT) View.GONE else View.VISIBLE
    }

    private fun clearSearchForm() {
        searchBinding.inputEditText.setText("")
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