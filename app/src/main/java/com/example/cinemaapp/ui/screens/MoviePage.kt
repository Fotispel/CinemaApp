package com.example.cinemaapp.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cinemaapp.viewmodel.MovieViewModel
import androidx.core.net.toUri
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.cinemaapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviePage(movieUrl: String, navController: NavController, viewModel: MovieViewModel) {
    val movies by viewModel.movies.collectAsState()

    val movie = movies.find { it.basicInfo.MovieURL == movieUrl }

    LaunchedEffect(movieUrl) {
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


    Scaffold(
        topBar = { /* TopAppBar όπως πριν */ }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Movie Title
            Text(
                text = title,
                fontFamily = ubuntuBold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                        val age_number = movie?.fullInfo?.ageRating?.filter { it.isDigit() }
                        val age_text = if (!age_number.isNullOrEmpty()) "Κ$age_number" else "Κ"
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = age_text,
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
                        val room = movie?.fullInfo?.projectionRoom ?: "--"
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = room,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                fontFamily = ubuntuMedium,
                            )
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

            // Movie Details
            val info = movie?.fullInfo


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
                            if (info.description.isNotEmpty())
                                Text(
                                    text = "Yπόθεση",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = ubuntuMedium
                                )
                            Text(
                                text = (info.description + "\n"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = ubuntuItalic
                            )
                            if (info.genre.isNotEmpty())
                                if (!info.genre.isNullOrEmpty()) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontFamily = ubuntuMedium)) {
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
                                    withStyle(
                                        style = SpanStyle(
                                            fontFamily = ubuntuMedium
                                        )
                                    ) {
                                        append("Πρεμιέρα: ")
                                    }
                                    append(info.premiereDate)
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Log.d("MoviePage", "Premiere Date: ${info?.premiereDate}")

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
                                fontFamily = ubuntuMedium
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            info.showtime.forEach { time ->
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = ubuntuRegular
                                )
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
                                        withStyle(style = SpanStyle(fontFamily = ubuntuMedium)) {
                                            append("Σκηνοθέτης: ")
                                        }
                                        append(info.director)
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (info.cast.isNotEmpty()) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontFamily = ubuntuMedium)) {
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
        }
    }
}

