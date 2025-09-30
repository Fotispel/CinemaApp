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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cinemaapp.R
import com.example.cinemaapp.viewmodel.MovieViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviePage(movieUrl: String, navController: NavController, viewModel: MovieViewModel) {
    val movies by viewModel.movies.collectAsState()
    val movie = movies.find { it.basicInfo.MovieURL == movieUrl }
    val title = movie?.basicInfo?.title ?: "Φόρτωση..."

    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit, movieUrl) {
        systemUiController.setNavigationBarColor(color = Color.Transparent, darkIcons = true)
        viewModel.fetchDetailedMovieInfo(movieUrl)
    }

    val ubuntuMedium = FontFamily(Font(R.font.ubuntu_medium, weight = FontWeight.W500))
    val ubuntuRegular = FontFamily(Font(R.font.ubuntu_regular, weight = FontWeight.Normal))
    val ubuntuItalic = FontFamily(Font(R.font.ubuntu_italic, weight = FontWeight.Normal))

    val context = LocalContext.current
    val info = movie?.fullInfo

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = title,
                        fontFamily = ubuntuMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent, // Κάνει διαφανές το background
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0.dp), // Αφαιρεί το default padding για gesture/status bar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding) // τώρα δεν έχει επιπλέον padding
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // Poster + Age, Duration, Room
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val posterWidth = screenWidth / 2
                val posterHeight = posterWidth * 3 / 2

                Card(
                    modifier = Modifier.width(posterWidth).height(posterHeight),
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

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(posterHeight)
                ) {
                    // Age Card
                    Card(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        val age = movie?.fullInfo?.ageRating?.let {
                            when {
                                it.contains("18") -> "Κ18"
                                it.contains("δεκαπέντε") -> "Κ15"
                                it.contains("δώδεκα") -> "Κ12"
                                it.contains("οκτώ") -> "Κ8"
                                it.contains("όλους") -> "Κ"
                                else -> it
                            }
                        } ?: "-"
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = age,
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = ubuntuMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Duration Card
                    Card(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = movie?.fullInfo?.duration ?: "-",
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = ubuntuMedium,
                                textAlign = TextAlign.Center
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
                            var projectionRooms = "-"
                            if (movie?.basicInfo?.MovieURL?.contains("cinelandpantelis.gr") == true) {
                                projectionRooms = movie.fullInfo?.projectionRoom
                                    ?.trim()
                                    ?: "-"

                            } else if (movie?.basicInfo?.MovieURL?.contains("texnopolis.net") == true) {
                                val roomRaw = movie?.fullInfo?.projectionRoom
                                val room = roomRaw?.substringBefore("Αίθουσα")?.trim()
                                projectionRooms = if (room.isNullOrBlank()) "-" else room

                            } else if (movie?.basicInfo?.MovieURL?.contains("flix.gr") == true) {
                                val rooms = info?.showtime?.map { entry ->
                                    val raw = entry.getOrNull(0) ?: ""
                                    Regex("ΑΙΘΟΥΣΑ\\s+(\\d+)").findAll(raw)
                                        .map { it.groupValues[1].toInt() }
                                        .toList()
                                }?.flatten()
                                    ?.distinct()
                                    ?.sorted()

                                projectionRooms = when {
                                    rooms == null || rooms.isEmpty() -> "-"
                                    rooms.size == 1 -> "Αίθουσα ${rooms.first()}"
                                    else -> "Αίθουσες ${rooms.joinToString(", ")}"
                                }
                            }

                            AutoResizeText(
                                text = projectionRooms,
                                fontFamily = ubuntuMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trailer + Site buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = {
                        movie?.fullInfo?.trailerUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Trailer")
                }

                Button(
                    onClick = {
                        movie?.basicInfo?.MovieURL?.takeIf { it.isNotEmpty() }?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Site")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description, Genre, Premiere, Showtime, Director, Cast
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                info?.let { movieInfo ->
                    if (!movieInfo.description.isNullOrEmpty() || !movieInfo.genre.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!movieInfo.description.isNullOrEmpty()) {
                                    Text("Υπόθεση", style = MaterialTheme.typography.bodyLarge, fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(movieInfo.description, style = MaterialTheme.typography.bodyLarge, fontFamily = ubuntuItalic)
                                }
                                if (!movieInfo.genre.isNullOrEmpty()) {
                                    Text(buildAnnotatedString {
                                        withStyle(SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                            append("Είδος: ")
                                        }
                                        append(movieInfo.genre)
                                    }, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }

                    if (!movieInfo.premiereDate.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(buildAnnotatedString {
                                    withStyle(SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                        append("Πρεμιέρα: ")
                                    }
                                    append(movieInfo.premiereDate)
                                }, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    if (!movieInfo.showtime.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Ώρες προβολής:", style = MaterialTheme.typography.bodyLarge, fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)
                                movieInfo.showtime.forEach { entry ->
                                    val dateTime = entry.getOrNull(0) ?: "-"
                                    Text(dateTime, style = MaterialTheme.typography.bodyLarge, fontFamily = ubuntuRegular)
                                }
                            }
                        }
                    }

                    if (!movieInfo.director.isNullOrEmpty() || !movieInfo.cast.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!movieInfo.director.isNullOrEmpty()) {
                                    Text(buildAnnotatedString {
                                        withStyle(SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                            append("Σκηνοθέτης: ")
                                        }
                                        append(movieInfo.director)
                                    }, style = MaterialTheme.typography.bodyLarge)
                                }
                                if (!movieInfo.cast.isNullOrEmpty()) {
                                    Text(buildAnnotatedString {
                                        withStyle(SpanStyle(fontFamily = ubuntuMedium, color = MaterialTheme.colorScheme.primary)) {
                                            append("Ηθοποιοί: ")
                                        }
                                        append(movieInfo.cast.joinToString(", "))
                                    }, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AutoResizeText(
    text: String,
    fontFamily: FontFamily,
    maxFontSize: TextUnit = 22.sp,
    minFontSize: TextUnit = 12.sp,
    modifier: Modifier = Modifier.padding(10.dp)
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
