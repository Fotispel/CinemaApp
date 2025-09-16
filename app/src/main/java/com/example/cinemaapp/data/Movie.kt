package com.example.cinemaapp.data

import java.util.Collections.emptyList

data class MovieBasicInfo(
    val title: String = "",
    val posterUrl: String = "",
    val MovieURL: String = "",
    val isPlaying: Boolean = true
)

data class FullMovieInfo(
    val title: String = "",
    val posterUrl: String = "",
    val duration: String = "",
    val genre: String = "",
    val ageRating: String = "",
    val projectionRoom: String = "",
    val description: String = "",
    val director: String = "",
    val cast: List<String> = emptyList(),
    val trailerUrl: String = "",
    val showtime: List<String> = emptyList(),
    val premiereDate: String = ""
)

data class Movie(
    val basicInfo: MovieBasicInfo,
    val fullInfo: FullMovieInfo? = null
)