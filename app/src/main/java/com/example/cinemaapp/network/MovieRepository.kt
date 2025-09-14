package com.example.cinemaapp.network

import com.example.cinemaapp.data.Movie
import com.example.cinemaapp.data.MovieBasicInfo
import com.example.cinemaapp.data.ComingSoonMovieInfo
import org.jsoup.Jsoup
import java.net.URL

class MovieRepository {

    fun fetchMovies(url: String): List<Movie> {
        val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
        val elements = doc.select("div.item")

        return elements.map { element ->
            val title = element.selectFirst("figcaption a")?.text()?.trim() ?: "Unknown"
            val posterUrl = "https://cinelandpantelis.gr/" + element.selectFirst("img.item__img")?.attr("src") ?: ""
            val premiereText = element.selectFirst("span.see-more")?.ownText()?.trim()
                ?.replace("Πρεμιέρα:", "") ?: ""

            Movie(
                basicInfo = MovieBasicInfo(title, posterUrl),
                comingSoonInfo = ComingSoonMovieInfo(premiereText)
            )
        }
    }
}
