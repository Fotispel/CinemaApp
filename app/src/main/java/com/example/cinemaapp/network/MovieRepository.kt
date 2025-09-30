package com.example.cinemaapp.network

import android.util.Log
import com.example.cinemaapp.data.Movie
import com.example.cinemaapp.data.MovieBasicInfo
import com.example.cinemaapp.data.FullMovieInfo
import org.jsoup.Jsoup

class MovieRepository {

    fun fetchMovies(url: String): List<Movie> {
        return when {
            url.contains("cinelandpantelis.gr") -> fetchMoviesFromCineland(url)
            url.contains("texnopolis.net") -> fetchMoviesFromTexnopolis(url)
            url.contains("flix.gr") -> fetchMoviesFromFlix(url)
            else -> {
                Log.w("MovieRepository", "Unknown URL: $url")
                emptyList()
            }
        }
    }

    fun fetchDetailedMovieInfo(movieUrl: String): Movie? {
        return when {
            movieUrl.contains("cinelandpantelis.gr") -> fetchDetailedCineland(movieUrl)
            movieUrl.contains("texnopolis.net") -> fetchDetailedTexnopolis(movieUrl)
            movieUrl.contains("flix.gr") -> fetchDetailedFlix(movieUrl)
            else -> null
        }
    }

    private fun fetchMoviesFromCineland(url: String): List<Movie> {
        Log.d("MovieRepository", "Fetching movies from URL: $url")
        val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
        val elements = doc.select("div.item")

        var isPlaying = true
        if (url == "https://cinelandpantelis.gr/prosechos.html")
            isPlaying = false

        return elements.map { element ->
            val title = element.selectFirst("figcaption a")?.text()?.trim() ?: "Unknown"
            val posterUrl =
                ("https://cinelandpantelis.gr/" + element.selectFirst("img.item__img")
                    ?.attr("src"))
            val moviePageRelative = element.selectFirst("figcaption a")?.attr("href") ?: ""
            val moviePageUrl = "https://cinelandpantelis.gr/$moviePageRelative"
            Log.d("MovieRepository", "Fetched movie: $title, IsPlaying: $isPlaying")
            Movie(
                basicInfo = MovieBasicInfo(title, posterUrl, moviePageUrl, isPlaying)
            )
        }
    }

    private fun fetchMoviesFromTexnopolis(url: String): List<Movie> {
        Log.d("MovieRepository", "Fetching movies from Texnopolis")
        val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
        val movies = mutableListOf<Movie>()

        val currentMovies = doc.select("section.currentMoviesSlider article.movieBox")
        currentMovies.forEach { element ->
            val title = element.selectFirst("h3.movieTitle")?.text()?.trim() ?: "Unknown"

            val posterUrl = element.selectFirst("img.wp-post-image")?.attr("data-src")
                ?: element.selectFirst("img.wp-post-image")?.attr("src")
                ?: ""

            val moviePageUrl = element.selectFirst("a[href]")?.attr("href") ?: ""

            movies.add(
                Movie(
                    basicInfo = MovieBasicInfo(
                        title = title,
                        posterUrl = posterUrl,
                        MovieURL = moviePageUrl,
                        isPlaying = true
                    )
                )
            )
        }


        val comingSoonMovies = doc.select("section.comingSoon_container article.upcoming_movie")
        Log.d(
            "MovieRepository",
            "Found coming soon sections: " + doc.select("section[class*=comingSoon_container]").size
        )

        comingSoonMovies.forEach { element ->
            val title = element.selectFirst("h3.movieTitle")?.text()?.trim() ?: "Unknown"

            val posterUrl = element.selectFirst("img.wp-post-image")?.attr("data-src")
                ?: element.selectFirst("img.wp-post-image")?.attr("src")
                ?: ""

            val moviePageUrl = element.selectFirst("a[href]")?.attr("href") ?: ""

            movies.add(
                Movie(
                    basicInfo = MovieBasicInfo(
                        title = title,
                        posterUrl = posterUrl,
                        MovieURL = moviePageUrl,
                        isPlaying = false
                    )
                )
            )
        }

        return movies
    }

    private fun fetchMoviesFromFlix(url: String): List<Movie> {
        val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get()
        val movieElements = doc.select("ul.list > li._subtle-border-bottom")

        return movieElements.map { element ->
            val title = element.selectFirst("h2 a .title")?.text()?.trim() ?: "Unknown"
            val posterUrl = element.selectFirst("img")?.attr("src") ?: ""

            val relativeUrl = element.selectFirst("h2 a")?.attr("href") ?: ""
            val movieUrl =
                if (relativeUrl.startsWith("http")) relativeUrl else "https://flix.gr$relativeUrl"

            // Προβολές
            val showtimes = element.select("dl.timetable dt, dl.timetable dd").map {
                it.text().trim()
            }.chunked(2).map { (room, time) -> "$room: $time" } // room + time

            Movie(
                basicInfo = MovieBasicInfo(
                    title = title,
                    posterUrl = posterUrl,
                    MovieURL = movieUrl,
                    isPlaying = true,
                    showtime = showtimes.map { listOf(it) }
                )
            )
        }
    }

    private fun fetchDetailedCineland(movieUrl: String): Movie? {
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
                val key =
                    row.select("td").firstOrNull()?.text()?.trim()?.removeSuffix(":") ?: ""
                val value = row.select("td").lastOrNull()?.text()?.trim() ?: ""
                if (key.isNotBlank()) {
                    info[key] = value
                }
            }

            val projectionRooms = doc.select("div.aithouses span.cinema").mapNotNull { span ->
                span.classNames().find { it.startsWith("Cineland") }
                    ?.replace("Cineland", "Αίθουσα ")
            }

            val projectionRoom = if (projectionRooms.isNotEmpty()) {
                projectionRooms.joinToString(", ")
            } else "-"


            val director = info["Σκηνοθεσία"] ?: ""

            val duration = info["Διάρκεια"] ?: "-"

            val genre = info["Είδος ταινίας"] ?: ""

            val releaseDateElement = doc.select("div.release_date span").last()
            val premiereDate = releaseDateElement?.text()?.trim()

            val description =
                doc.selectFirst("div.ce_text.block p[style]")?.text()?.trim() ?: ""
            val trailerUrl = doc.selectFirst("div.ce_youtube iframe")?.attr("src") ?: ""


            val actorRow = doc.select("strong:contains(Ηθοποιοί:)").firstOrNull()?.closest("tr")
            val castText = actorRow?.select("td")?.getOrNull(2)?.text()?.trim() ?: ""
            val castList = castText.split(",").map { it.trim() }.filter { it.isNotEmpty() }


            val showtime = doc.select("div.ce_dma_eg_1 ul li.text, div.ce_dma_eg_2 ul li.text")
                .mapNotNull { li ->
                    val day =
                        li.selectFirst("span.label")?.text()?.trim() ?: return@mapNotNull null
                    val time =
                        li.selectFirst("span.value")?.text()?.trim() ?: return@mapNotNull null
                    listOf(day, time)
                }

            val ageRating = doc.selectFirst("div.rated span.rated-label")?.text()?.trim() ?: "-"

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

    private fun fetchDetailedTexnopolis(movieUrl: String): Movie? {
        return try {
            val doc = Jsoup.connect(movieUrl).userAgent("Mozilla/5.0").get()

            val title = doc.selectFirst("h1")?.text() ?: ""
            val posterUrl = doc.selectFirst(".movie_poster img")?.attr("data-src") ?: ""
            val projectionRoom = doc.selectFirst(".playingTheaters")?.text()?.trim() ?: ""
            val description = doc.selectFirst(".movie_content > p")?.text() ?: ""

            val shortInfo = doc.select(".movie_infoDetails__short li")
            val genre = shortInfo.getOrNull(0)?.text()?.trim() ?: ""
            val duration = shortInfo.getOrNull(1)?.text()?.trim() ?: ""
            val ageRating = shortInfo.find { it.text().contains("Καταλληλότητα") }?.text()
                ?.substringAfter("Καταλληλότητα")
                ?.trim()
                ?.trim(':')
                ?: "-"


            val longInfo = doc.select(".movie_infoDetails__long li")
            val director = longInfo.find { it.text().contains("Σκηνοθεσία:") }
                ?.text()
                ?.replace("Σκηνοθεσία:", "")
                ?.trim() ?: ""

            val castText = longInfo.find { it.text().contains("Ηθοποιοί:") }
                ?.text()
                ?.replace("Ηθοποιοί:", "")
                ?.trim() ?: ""
            val cast = castText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            shortInfo.forEachIndexed { index, li ->
                Log.d("MovieRepository", "shortInfo[$index]: ${li.text()}")
            }

            val trailerUrl =
                doc.selectFirst("div.movie_trailer iframe")?.attr("data-src-cmplz") ?: ""

            val showtime = mutableListOf<List<String>>()


            for (article in doc.select("article.movieBox")) {

                val movieTitleToFind =
                    article.selectFirst("div.movie_info__details h2.movieTitle")?.text()?.trim()
                        ?: continue

                if (title != movieTitleToFind) continue

                val showtimeElements = article.select("div.movieShows_day")

                showtimeElements.forEach { dayDiv ->
                    val day = dayDiv.selectFirst("h3")?.text()?.trim() ?: return@forEach
                    dayDiv.select("div.movieShows_day_theater").forEach { theaterDiv ->
                        var theater = theaterDiv.selectFirst("h4")?.text()?.trim() ?: "-"
                        theater = Regex("^(.*?Αίθουσα\\s*\\S+)").find(theater)?.value ?: theater

                        val times = theaterDiv.select("div.movieShows_showTimes span")
                            .joinToString(", ") { it.text().trim() }

                        showtime.add(listOf(day, times, theater))
                    }
                }
            }

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
                        cast = cast,
                        showtime = showtime,
                        trailerUrl = trailerUrl,
                        premiereDate = ""
                    )

                } else null
            )

        } catch (e: Exception) {
            null
        }
    }

    private fun fetchDetailedFlix(movieUrl: String): Movie? {
        return try {
            val doc = Jsoup.connect(movieUrl).userAgent("Mozilla/5.0").get()

            val basicMovie = fetchMovies("https://flix.gr/theatres/61")
                .find { it.basicInfo.MovieURL == movieUrl }


            // Τίτλος και poster
            val title = doc.selectFirst("h1, h2.title")?.text()?.trim() ?: "Unknown"
            val posterUrl = doc.selectFirst("div.w-sm-20 img.img-fluid")?.attr("src") ?: ""


            // Περιγραφή
            val description =
                doc.selectFirst("meta[name=description]")?.attr("content")?.trim() ?: ""

            // Σκηνοθέτης
            val director =
                doc.select("li:contains(Σκηνοθεσία)")?.firstOrNull()?.ownText()?.trim() ?: ""

            // Πρωταγωνιστούν
            val castText =
                doc.select("li:contains(Πρωταγωνιστούν)")?.firstOrNull()?.ownText()?.trim() ?: ""
            val cast = castText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // Διάρκεια
            val duration =
                doc.select("li:contains(Διάρκεια)")?.firstOrNull()?.ownText()?.trim() ?: "-"

            val trailerUrl = doc.selectFirst("iframe[src*='youtube.com']")?.attr("src") ?: ""

            val showtime = basicMovie?.basicInfo?.showtime ?: emptyList()

            val premiereDate =
                doc.selectFirst("meta[name=publish-date]")?.attr("content")?.substringBefore("T")
                    ?: ""

            Movie(
                basicInfo = MovieBasicInfo(
                    title = title,
                    posterUrl = posterUrl,
                    MovieURL = movieUrl,
                    isPlaying = true,
                    showtime = showtime
                ),
                fullInfo = FullMovieInfo(
                    title = title,
                    posterUrl = posterUrl,
                    duration = duration,
                    genre = "",
                    ageRating = "-",
                    projectionRoom = "",
                    description = description,
                    director = director,
                    cast = cast,
                    showtime = showtime,
                    trailerUrl = trailerUrl,
                    premiereDate = premiereDate
                )
            )


        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

