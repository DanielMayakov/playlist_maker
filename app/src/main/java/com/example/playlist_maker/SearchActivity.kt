package com.example.playlist_maker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlist_maker.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private val tracksBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(tracksBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val tracksApi = retrofit.create(TracksApi::class.java)
    private val adapter = TrackAdapter()

    companion object {
        const val SEARCH_QUERY = "SEARCH_QUERY"
    }

    lateinit var searchBinding: ActivitySearchBinding
    private var searchInputQuery = ""

    private val simpleTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, s1: Int, s2: Int, s3: Int) {
            if (s.isNullOrEmpty()) {
                searchBinding.clearImageView.visibility = GONE
            } else {
                searchBinding.clearImageView.visibility = VISIBLE
            }
            searchInputQuery = s.toString()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(searchBinding.root)

        searchBinding.inputEditText.requestFocus()

        searchBinding.settingsToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        searchBinding.clearImageView.setOnClickListener { clearSearchForm() }
        searchBinding.inputEditText.addTextChangedListener(simpleTextWatcher)

        searchBinding.searchRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        searchBinding.searchRecycler.adapter = adapter

        searchBinding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchTrack()
            }
            false
        }
        refresh()
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
                    if (searchBinding.inputEditText.text.isNotEmpty() && !response.body()?.results.isNullOrEmpty() && response.code() == 200) {
                        adapter.tracks = response.body()?.results as MutableList<Track>
                        showPlaceholder(Placeholder.SEARCH_RESULT)
                    } else {
                        showPlaceholder(Placeholder.NOTHING_FOUND)
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    showPlaceholder(Placeholder.INTERNET_PROBLEM)
                }
            })
    }

    fun showPlaceholder(placeholder: Placeholder) {
        when (placeholder) {
            Placeholder.NOTHING_FOUND -> {
                searchBinding.searchRecycler.visibility = GONE
                searchBinding.internetProblem.visibility = GONE
                searchBinding.nothingFound.visibility = VISIBLE

            }
            Placeholder.INTERNET_PROBLEM -> {
                searchBinding.searchRecycler.visibility = GONE
                searchBinding.nothingFound.visibility = GONE
                searchBinding.internetProblem.visibility = VISIBLE
            }

            else -> {
                searchBinding.searchRecycler.visibility = VISIBLE
                searchBinding.nothingFound.visibility = GONE
                searchBinding.internetProblem.visibility = GONE
            }
        }
    }

    private fun clearSearchForm() {
        searchBinding.inputEditText.setText("")
        adapter.tracks.clear()
        showPlaceholder(Placeholder.SEARCH_RESULT)
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