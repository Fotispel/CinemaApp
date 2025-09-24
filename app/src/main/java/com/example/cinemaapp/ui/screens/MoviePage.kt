package com.example.cinemaapp.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cinemaapp.viewmodel.MovieViewModel
import androidx.core.net.toUri
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.cinemaapp.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviePage(movieUrl: String, navController: NavController, viewModel: MovieViewModel) {
    val movies by viewModel.movies.collectAsState()

    val movie = movies.find { it.basicInfo.MovieURL == movieUrl }

    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit, movieUrl) {
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        viewModel.fetchDetailedMovieInfo(movieUrl)
    }

    val title = movie?.basicInfo?.title ?: "Φόρτωση..."
    val ubuntuMedium = FontFamily(Font(R.font.ubuntu_medium, weight = FontWeight.W500))
    val ubuntuBold = FontFamily(Font(R.font.ubuntu_bold, weight = FontWeight.Bold))
    val ubuntuRegular = FontFamily(Font(R.font.ubuntu_regular, weight = FontWeight.Normal))
    val ubuntuLight = FontFamily(Font(R.font.ubuntu_light, weight = FontWeight.Light))
    val ubuntuItalic = FontFamily(Font(R.font.ubuntu_italic, weight = FontWeight.Normal))
    val ubuntuBoldItalic = FontFamily(Font(R.font.ubuntu_bolditalic, weight = FontWeight.Bold))
    val ubuntuMediumItalic =
        FontFamily(Font(R.font.ubuntu_mediumitalic, weight = FontWeight.Medium))
    val ubuntuLightItalic = FontFamily(Font(R.font.ubuntu_lightitalic, weight = FontWeight.Light))


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopAppBar
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontFamily = ubuntuMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                // Movie Details
                val info = movie?.fullInfo

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    val posterWidth = screenWidth / 2
                    val posterHeight = posterWidth * 3 / 2

                    Card(
                        modifier = Modifier
                            .width(posterWidth)
                            .height(posterHeight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        if (movie != null && movie.basicInfo.posterUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = movie.basicInfo.posterUrl),
                                contentDescription = movie.basicInfo.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Poster της ταινίας")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Age, Duration & Room Cards
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(posterHeight),
                    ) {
                        // Age
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            var age = ""
                            if (movie?.basicInfo?.MovieURL?.contains("cinelandpantelis.gr") == true) {
                                val age_number = movie.fullInfo?.ageRating?.filter { it.isDigit() }
                                age =
                                    if (!age_number.isNullOrEmpty()) "Κ$age_number" else "Κ"
                            } else {
                                age = movie?.fullInfo?.ageRating ?: "-"
                                if (age == " Κατάλληλη άνω των 18 ετών")
                                    age = "Κ18"
                                else if (age == " Κατάλληλη για άνω των δεκαπέντε")
                                    age = "Κ15"
                                else if (age == " Κατάλληλη για άνω των δώδεκα")
                                    age = "Κ12"
                                else if (age == " Κατάλληλη για άνω των οκτώ")
                                    age = "Κ8"
                                else if (age == " Κατάλληλη για όλους")
                                    age = "Κ"
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = age,
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    fontFamily = ubuntuMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Duration
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            val duration =
                                movie?.fullInfo?.duration
                                    ?: "--"
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = duration,
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    fontFamily = ubuntuMedium
                                )
                            }
                        }

                        // Room
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (movie?.basicInfo?.MovieURL?.contains("cinelandpantelis.gr") == true) {
                                    val room = movie.fullInfo?.projectionRoom
                                        ?.trim()
                                        ?: "-"

                                    AutoResizeText(
                                        text = room,
                                        fontFamily = ubuntuMedium
                                    )
                                } else {
                                    val roomRaw = movie?.fullInfo?.projectionRoom
                                    val room = roomRaw?.substringBefore("Αίθουσα")?.trim()
                                    val roomDisplay = if (room.isNullOrBlank()) "-" else room

                                    AutoResizeText(
                                        text = roomDisplay,
                                        fontFamily = ubuntuMedium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val context = LocalContext.current
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val trailerUrl = movie?.fullInfo?.trailerUrl ?: ""
                            Log.d("MoviePage", "Trailer URL: $trailerUrl")
                            if (trailerUrl.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, trailerUrl.toUri())
                                context.startActivity(intent)
                            }
                        },
                        shape = MaterialTheme.shapes.extraLarge, // pill shape
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Trailer")
                    }

                    Button(
                        onClick = {
                            val moviePageUrl = movie?.basicInfo?.MovieURL
                            if (moviePageUrl?.isNotEmpty() ?: false) {
                                val intent = Intent(Intent.ACTION_VIEW, moviePageUrl.toUri())
                                context.startActivity(intent)
                            }
                        },
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Site")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!info?.description.isNullOrEmpty() || !info?.genre.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (info.description.isNotEmpty()) {
                                    Text(
                                        text = "Υπόθεση",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = ubuntuMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = (info.description + "\n"),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = ubuntuItalic
                                    )
                                }
                                if (info.genre.isNotEmpty()) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                                append("Είδος: ")
                                            }
                                            append(info.genre)
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    if (!info?.premiereDate.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                            append("Πρεμιέρα: ")
                                        }
                                        append(info.premiereDate)
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    if (!info?.showtime.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Ώρες προβολής:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = ubuntuMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )


                                Spacer(modifier = Modifier.height(3.dp))

                                if (movie.basicInfo.MovieURL.contains("cinelandpantelis.gr")) {
                                    info.showtime.forEach { entry ->
                                        // entry[0] = day, entry[1] = time
                                        val day = entry.getOrNull(0) ?: "-"
                                        val time = entry.getOrNull(1) ?: "-"
                                        Text(
                                            text = "$day $time",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontFamily = ubuntuRegular
                                        )
                                    }
                                } else {
                                    val groupedByCinema = info.showtime.groupBy { entry ->
                                        val theaterFull = entry.getOrNull(2) ?: "-"
                                        theaterFull.substringBefore("Αίθουσα").trim()
                                    }

                                    groupedByCinema.forEach { (cinemaName, entries) ->
                                        Text(
                                            text = cinemaName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontFamily = ubuntuMedium
                                        )

                                        entries.take(10).forEach { entry ->
                                            val day = entry.getOrNull(0) ?: "-"
                                            val time = entry.getOrNull(1) ?: "-"
                                            val theater = entry.getOrNull(2) ?: "-"
                                            val theaterOnly =
                                                theater.substringAfterLast("Αίθουσα").trim()
                                            val theaterFormatted =
                                                if (theaterOnly.isNotEmpty()) "Αίθουσα $theaterOnly" else "-"

                                            if (!theaterFormatted.contains("Θερινό")) {
                                                Text(
                                                    text = " $day $time ($theaterFormatted)",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontFamily = ubuntuRegular
                                                )
                                            } else {
                                                Text(
                                                    text = " $day $time",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontFamily = ubuntuRegular
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (!info?.director.isNullOrEmpty() || !info?.cast.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (info.director.isNotEmpty()) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                                append("Σκηνοθέτης: ")
                                            }
                                            append(info.director)
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                if (info.cast.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                                append("Ηθοποιοί: ")
                                            }
                                            append(info.cast.joinToString(", "))
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


@Composable
fun AutoResizeText(
    text: String,
    fontFamily: FontFamily,
    maxFontSize: TextUnit = 22.sp,
    minFontSize: TextUnit = 12.sp,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }

    Text(
        text = text,
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = TextAlign.Center,
        softWrap = true,
        maxLines = Int.MAX_VALUE,
        modifier = modifier.fillMaxWidth(),
        onTextLayout = { layoutResult ->
            if (layoutResult.hasVisualOverflow && fontSize > minFontSize) {
                fontSize *= 0.9f
            }
        }
    )
}


