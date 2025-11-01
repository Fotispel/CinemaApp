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
import kotlinx.coroutines.withContext

class MovieViewModel(initialCinema: String = "Odeon") : ViewModel() {

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
        fetchAllMovies(initialCinema)
    }


    fun selectCinema(cinema: String) {
        _selectedCinema.value = cinema

        _nowPlayingMovies.value = emptyList()
        _comingSoonMovies.value = emptyList()
        _movies.value = emptyList()

        _isLoading.value = true

        fetchAllMovies(cinema)
    }



    fun fetchAllMovies(cinema: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (nowPlayingList, comingSoonList) = withContext(Dispatchers.IO) {
                    when (cinema) {
                        "Pantelis" -> {
                            val now = repository.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
                            val soon = repository.fetchMovies("https://cinelandpantelis.gr/prosechos.html")
                            now to soon
                        }
                        "Texnopolis" -> {
                            val all = repository.fetchMovies("https://www.texnopolis.net/movies/")
                            val now = all.filter { it.basicInfo.isPlaying }
                            val soon = all.filter { !it.basicInfo.isPlaying }
                            now to soon
                        }
                        "Odeon" -> {
                            val now = repository.fetchMovies("https://flix.gr/theatres/61")
                            now to emptyList()
                        }
                        else -> emptyList<Movie>() to emptyList()
                    }
                }

                _nowPlayingMovies.value = nowPlayingList
                _comingSoonMovies.value = comingSoonList
                _movies.value= nowPlayingList + comingSoonList

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

    fun refreshNowPlayingMovies(cinema: String = _selectedCinema.value) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nowPlayingList = when (cinema) {
                    "Pantelis" -> repository.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
                    "Texnopolis" -> {
                        val allMovies = repository.fetchMovies("https://www.texnopolis.net/movies/")
                        allMovies.filter { it.basicInfo.isPlaying }
                    }
                    "Odeon" -> repository.fetchMovies("https://flix.gr/theatres/61")
                    else -> emptyList()
                }
                _nowPlayingMovies.value = nowPlayingList
                updateCombinedMovies()
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error refreshing now playing movies for $cinema", e)
            }
        }
    }


    fun refreshComingSoonMovies(cinema: String = _selectedCinema.value) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val comingSoonList = when (cinema) {
                    "Pantelis" -> repository.fetchMovies("https://cinelandpantelis.gr/prosechos.html")
                    "Texnopolis" -> {
                        val allMovies = repository.fetchMovies("https://www.texnopolis.net/movies/")
                        allMovies.filter { !it.basicInfo.isPlaying }
                    }
                    else -> emptyList()
                }
                _comingSoonMovies.value = comingSoonList
                updateCombinedMovies()
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error refreshing coming soon movies for $cinema", e)
            }
        }
    }


    fun fetchDetailedMovieInfo(movieUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val detailedMovie = repository.fetchDetailedMovieInfo(movieUrl)

            Log.d("MovieViewModel", "Movie info: $detailedMovie")

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
