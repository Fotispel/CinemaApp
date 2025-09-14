package com.example.cinemaapp.data

import java.util.Collections.emptyList

data class MovieBasicInfo(
    val title: String = "",
    val posterUrl: String = "",
    val duration: Int = 0,
    val genre: String = "",
    val ageRating: String = "",
    val projectionRoom: String = "",
    val description: String = "",
    val director: String = "",
    val cast: List<String> = emptyList(),
    val trailerUrl: String = "",
)

data class NowPlayingMovieInfo(
    val showtime: List<String> = emptyList()
)

data class ComingSoonMovieInfo(
    val premiereDate: String = ""
)

data class Movie(
    val basicInfo: MovieBasicInfo,
    val nowPlayingInfo: NowPlayingMovieInfo? = null,
    val comingSoonInfo: ComingSoonMovieInfo? = null
)