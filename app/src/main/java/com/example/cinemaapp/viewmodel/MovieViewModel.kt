package com.example.cinemaapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinemaapp.data.Movie
import com.example.cinemaapp.network.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {

    private val repository = MovieRepository()

    // Separate state flows for now playing and coming soon movies
    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val nowPlayingMovies: StateFlow<List<Movie>> = _nowPlayingMovies

    private val _comingSoonMovies = MutableStateFlow<List<Movie>>(emptyList())
    val comingSoonMovies: StateFlow<List<Movie>> = _comingSoonMovies

    // Combined movies for backward compatibility
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Fetch all movies at startup
        fetchAllMovies()
    }

    fun fetchAllMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val nowPlayingList = repository.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
                val comingSoonList = repository.fetchMovies("https://cinelandpantelis.gr/prosechos.html")
                
                _nowPlayingMovies.value = nowPlayingList
                _comingSoonMovies.value = comingSoonList
                _movies.value = nowPlayingList + comingSoonList
                
                Log.d("MovieViewModel", "Fetched ${nowPlayingList.size} now playing and ${comingSoonList.size} coming soon movies")
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error fetching movies", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshNowPlayingMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nowPlayingList = repository.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
                _nowPlayingMovies.value = nowPlayingList
                updateCombinedMovies()
                Log.d("MovieViewModel", "Refreshed ${nowPlayingList.size} now playing movies")
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error refreshing now playing movies", e)
            }
        }
    }

    fun refreshComingSoonMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val comingSoonList = repository.fetchMovies("https://cinelandpantelis.gr/prosechos.html")
                _comingSoonMovies.value = comingSoonList
                updateCombinedMovies()
                Log.d("MovieViewModel", "Refreshed ${comingSoonList.size} coming soon movies")
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error refreshing coming soon movies", e)
            }
        }
    }

    fun refreshAllMovies() {
        fetchAllMovies()
    }

    private fun updateCombinedMovies() {
        _movies.value = _nowPlayingMovies.value + _comingSoonMovies.value
    }

    // Legacy method for backward compatibility
    fun fetchMovies(url: String) {
        when (url) {
            "https://cinelandpantelis.gr/proballontai.html" -> refreshNowPlayingMovies()
            "https://cinelandpantelis.gr/prosechos.html" -> refreshComingSoonMovies()
            else -> fetchAllMovies()
        }
    }

    fun fetchDetailedMovieInfo(movieUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val detailedMovie = repository.fetchDetailedMovieInfo(movieUrl)

            Log.d("MovieViewModel", "Detailed movie fetched: $detailedMovie")

            if (detailedMovie != null) {
                // Update in now playing movies
                val currentNowPlaying = _nowPlayingMovies.value.toMutableList()
                val nowPlayingIndex = currentNowPlaying.indexOfFirst { it.basicInfo.MovieURL == detailedMovie.basicInfo.MovieURL }
                if (nowPlayingIndex != -1) {
                    currentNowPlaying[nowPlayingIndex] = detailedMovie
                    _nowPlayingMovies.value = currentNowPlaying
                }

                // Update in coming soon movies
                val currentComingSoon = _comingSoonMovies.value.toMutableList()
                val comingSoonIndex = currentComingSoon.indexOfFirst { it.basicInfo.MovieURL == detailedMovie.basicInfo.MovieURL }
                if (comingSoonIndex != -1) {
                    currentComingSoon[comingSoonIndex] = detailedMovie
                    _comingSoonMovies.value = currentComingSoon
                }

                // Update combined movies
                updateCombinedMovies()
            }
        }
    }
}
