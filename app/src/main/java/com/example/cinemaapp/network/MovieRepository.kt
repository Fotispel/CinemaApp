package com.example.cinemaapp.network

import com.example.cinemaapp.data.Movie
import com.example.cinemaapp.data.MovieBasicInfo
import com.example.cinemaapp.data.ComingSoonMovieInfo
import com.example.cinemaapp.data.NowPlayingMovieInfo
import org.jsoup.Jsoup
import java.net.URL

class MovieRepository {

    fun fetchMovies(url: String): List<Movie> {
        val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
        val elements = doc.select("div.item")

        return elements.map { element ->
            val title = element.selectFirst("figcaption a")?.text()?.trim() ?: "Unknown"
            val posterUrl = "https://cinelandpantelis.gr/" + element.selectFirst("img.item__img")?.attr("src") ?: ""
            val moviePageUrl = element.selectFirst("figure.image_container a")?.attr("href") ?: ""

            Movie(
                basicInfo = MovieBasicInfo(title, posterUrl, moviePageUrl)
            )
        }
    }

    fun fetchDetailedMovieInfo(movieUrl: String): Movie? {
        return try {
            val fullUrl = if (movieUrl.startsWith("http")) movieUrl else "https://cinelandpantelis.gr$movieUrl"
            val doc = Jsoup.connect(fullUrl).userAgent("Mozilla/5.0").get()
            
            val title = doc.selectFirst("h1")?.text()?.trim() ?: ""
            val posterElement = doc.selectFirst("img[src*='poster'], img[src*='movie']")
            val posterUrl = if (posterElement != null) {
                val src = posterElement.attr("src")
                if (src.startsWith("http")) src else "https://cinelandpantelis.gr$src"
            } else ""
            
            // Try to extract additional information from the page
            val description = doc.selectFirst("div.description, p.description")?.text()?.trim() ?: ""
            val director = doc.selectFirst("span:contains(Σκηνοθέτης), div:contains(Σκηνοθέτης)")?.text()?.trim() ?: ""
            val duration = doc.selectFirst("span:contains(Διάρκεια), div:contains(Διάρκεια)")?.text()?.trim() ?: ""
            val genre = doc.selectFirst("span:contains(Είδος), div:contains(Είδος)")?.text()?.trim() ?: ""
            val ageRating = doc.selectFirst("span:contains(Ηλικία), div:contains(Ηλικία)")?.text()?.trim() ?: ""
            
            Movie(
                basicInfo = MovieBasicInfo(title, posterUrl, movieUrl),
                nowPlayingInfo = if (description.isNotEmpty() || director.isNotEmpty()) {
                    NowPlayingMovieInfo(
                        title = title,
                        posterUrl = posterUrl,
                        duration = duration,
                        genre = genre,
                        ageRating = ageRating,
                        description = description,
                        director = director
                    )
                } else null
            )
        } catch (e: Exception) {
            null
        }
    }
}
