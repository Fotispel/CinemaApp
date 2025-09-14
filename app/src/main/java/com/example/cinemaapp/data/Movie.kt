package com.example.cinemaapp.data

import java.util.Collections.emptyList

data class MovieBasicInfo(
    val title: String = "",
    val posterUrl: String = "",
    val MovieURL: String = ""
)

data class NowPlayingMovieInfo(
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
    val showtime: List<String> = emptyList()
    )

data class ComingSoonMovieInfo(
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
    val premiereDate: String = ""
    )

data class Movie(
    val basicInfo: MovieBasicInfo,
    val nowPlayingInfo: NowPlayingMovieInfo? = null,
    val comingSoonInfo: ComingSoonMovieInfo? = null
)