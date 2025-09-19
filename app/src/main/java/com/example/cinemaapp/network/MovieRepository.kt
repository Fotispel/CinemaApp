package com.example.cinemaapp.network

import android.util.Log
import com.example.cinemaapp.data.Movie
import com.example.cinemaapp.data.MovieBasicInfo
import com.example.cinemaapp.data.FullMovieInfo
import org.jsoup.Jsoup

class MovieRepository {

    fun fetchMovies(url: String): List<Movie> {
        val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
        val elements = doc.select("div.item")

        var isPlaying = true
        if (url == "https://cinelandpantelis.gr/prosechos.html")
            isPlaying = false

        return elements.map { element ->
            val title = element.selectFirst("figcaption a")?.text()?.trim() ?: "Unknown"
            val posterUrl =
                ("https://cinelandpantelis.gr/" + element.selectFirst("img.item__img")?.attr("src"))
            val moviePageRelative = element.selectFirst("figcaption a")?.attr("href") ?: ""
            val moviePageUrl = "https://cinelandpantelis.gr/$moviePageRelative"
            Log.d("MovieRepository", "Fetched movie: $title, IsPlaying: $isPlaying")
            Movie(
                basicInfo = MovieBasicInfo(title, posterUrl, moviePageUrl, isPlaying)
            )
        }
    }

    fun fetchDetailedMovieInfo(movieUrl: String): Movie? {
        Log.d("MovieRepository", "Fetching detailed info for URL: $movieUrl")
        return try {
            val fullUrl =
                if (movieUrl.startsWith("http")) movieUrl else "https://cinelandpantelis.gr/$movieUrl"
            val doc = Jsoup.connect(fullUrl).userAgent("Mozilla/5.0").get()

            val title = doc.selectFirst("h1")?.text()?.trim() ?: ""
            val posterElement = doc.selectFirst("img[src*='poster'], img[src*='movie']")
            val posterUrl = if (posterElement != null) {
                val src = posterElement.attr("src")
                if (src.startsWith("http")) src else "https://cinelandpantelis.gr/$src"
            } else ""

            val info = mutableMapOf<String, String>()

            doc.select("table tr").forEach { row ->
                val key = row.select("td").firstOrNull()?.text()?.trim()?.removeSuffix(":") ?: ""
                val value = row.select("td").lastOrNull()?.text()?.trim() ?: ""
                if (key.isNotBlank()) {
                    info[key] = value
                }
            }

            val projectionRooms = doc.select("div.aithouses span.cinema").mapNotNull { span ->
                span.classNames().find { it.startsWith("Cineland") }?.replace("Cineland", "Αίθουσα ")
            }

            val projectionRoom = if (projectionRooms.isNotEmpty()) {
                projectionRooms.joinToString(", ")
            } else "-"


            val director = info["Σκηνοθεσία"] ?: ""

            val duration = info["Διάρκεια"] ?: "-"

            val genre = info["Είδος ταινίας"] ?: ""

            val releaseDateElement = doc.select("div.release_date span").last()
            val premiereDate = releaseDateElement?.text()?.trim()

            val description = doc.selectFirst("div.ce_text.block p[style]")?.text()?.trim() ?: ""
            val trailerUrl = doc.selectFirst("div.ce_youtube iframe")?.attr("src") ?: ""


            val actorRow = doc.select("strong:contains(Ηθοποιοί:)").firstOrNull()?.closest("tr")
            val castText = actorRow?.select("td")?.getOrNull(2)?.text()?.trim() ?: ""
            val castList = castText.split(",").map { it.trim() }.filter { it.isNotEmpty() }


            val showtime = doc.select("div.ce_dma_eg_1 ul li.text, div.ce_dma_eg_2 ul li.text").mapNotNull { li ->
                val day = li.selectFirst("span.label")?.text()?.trim() ?: return@mapNotNull null
                val time = li.selectFirst("span.value")?.text()?.trim() ?: return@mapNotNull null
                "$day: $time"
            }


            val ageRating = doc.selectFirst("div.rated span.rated-label")?.text()?.trim() ?: "-"


            Log.d("MovieRepository", "Description: $description")
            Movie(
                basicInfo = MovieBasicInfo(title, posterUrl, movieUrl),
                fullInfo = if (description.isNotEmpty() || director.isNotEmpty()) {
                    FullMovieInfo(
                        title = title,
                        posterUrl = posterUrl,
                        duration = duration,
                        genre = genre,
                        ageRating = ageRating,
                        projectionRoom = projectionRoom,
                        description = description,
                        director = director,
                        cast = castList,
                        showtime = showtime,
                        trailerUrl = trailerUrl,
                        premiereDate = premiereDate ?: ""
                    )

                } else null
            )

        } catch (e: Exception) {
            null
        }
    }
}
