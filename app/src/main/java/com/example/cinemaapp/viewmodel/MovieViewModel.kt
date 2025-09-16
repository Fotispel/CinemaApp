package com.example.cinemaapp.viewmodel

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

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    fun fetchMovies(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val movieList = repository.fetchMovies(url)
            _movies.value = movieList
        }
    }

    fun fetchDetailedMovieInfo(movieUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val detailedMovie = repository.fetchDetailedMovieInfo(movieUrl)
            if (detailedMovie != null) {
                val currentMovies = _movies.value.toMutableList()
                val index = currentMovies.indexOfFirst { it.basicInfo.title == detailedMovie.basicInfo.title }
                if (index != -1) {
                    currentMovies[index] = detailedMovie
                    _movies.value = currentMovies
                }
            }
        }
    }
}
