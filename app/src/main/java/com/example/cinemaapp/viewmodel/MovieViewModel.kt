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

class MovieViewModel(initialCinema: String = "Pantelis") : ViewModel() {

    private val repository = MovieRepository()

    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val nowPlayingMovies: StateFlow<List<Movie>> = _nowPlayingMovies

    private val _comingSoonMovies = MutableStateFlow<List<Movie>>(emptyList())
    val comingSoonMovies: StateFlow<List<Movie>> = _comingSoonMovies

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedCinema = MutableStateFlow(initialCinema)
    val selectedCinema: StateFlow<String> = _selectedCinema

    init {
        // Αυτόματα φορτώνει τις ταινίες με βάση το αρχικό cinema
        viewModelScope.launch {
            _selectedCinema.collect { cinema ->
                fetchAllMovies(cinema)
            }
        }
    }

    fun selectCinema(cinema: String) {
        _selectedCinema.value = cinema
    }

    fun fetchAllMovies(cinema: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                if (cinema == "Pantelis") {
                    val nowPlayingList = repository.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
                    val comingSoonList = repository.fetchMovies("https://cinelandpantelis.gr/prosechos.html")
                    _nowPlayingMovies.value = nowPlayingList
                    _comingSoonMovies.value = comingSoonList
                    _movies.value = nowPlayingList + comingSoonList
                } else if (cinema == "Texnopolis") {
                    val allMovies = repository.fetchMovies("https://www.texnopolis.net/movies/")
                    val nowPlayingList = allMovies.filter { it.basicInfo.isPlaying }
                    val comingSoonList = allMovies.filter { !it.basicInfo.isPlaying }

                    _nowPlayingMovies.value = nowPlayingList
                    _comingSoonMovies.value = comingSoonList
                    _movies.value = allMovies
                } else {
                    Log.w("MovieViewModel", "Unknown cinema: $cinema")
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error fetching movies for $cinema", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateCombinedMovies() {
        _movies.value = _nowPlayingMovies.value + _comingSoonMovies.value
    }

    fun refreshNowPlayingMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nowPlayingList = repository.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
                _nowPlayingMovies.value = nowPlayingList
                updateCombinedMovies()
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
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error refreshing coming soon movies", e)
            }
        }
    }

    fun fetchDetailedMovieInfo(movieUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val detailedMovie = repository.fetchDetailedMovieInfo(movieUrl)
            if (detailedMovie != null) {
                val currentNowPlaying = _nowPlayingMovies.value.toMutableList()
                val nowPlayingIndex = currentNowPlaying.indexOfFirst { it.basicInfo.MovieURL == detailedMovie.basicInfo.MovieURL }
                if (nowPlayingIndex != -1) {
                    currentNowPlaying[nowPlayingIndex] = detailedMovie
                    _nowPlayingMovies.value = currentNowPlaying
                }

                val currentComingSoon = _comingSoonMovies.value.toMutableList()
                val comingSoonIndex = currentComingSoon.indexOfFirst { it.basicInfo.MovieURL == detailedMovie.basicInfo.MovieURL }
                if (comingSoonIndex != -1) {
                    currentComingSoon[comingSoonIndex] = detailedMovie
                    _comingSoonMovies.value = currentComingSoon
                }

                updateCombinedMovies()
            }
        }
    }
}
