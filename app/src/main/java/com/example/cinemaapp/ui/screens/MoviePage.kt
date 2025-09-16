package com.example.cinemaapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cinemaapp.data.Movie
import com.example.cinemaapp.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviePage(movieUrl: String, navController: NavController, viewModel: MovieViewModel) {
    // Λίστα ταινιών που κρατάει το ViewModel
    val movies by viewModel.movies.collectAsState()

    // Προσπάθησε να βρεις την ταινία με βάση το URL
    val movie = movies.find { it.basicInfo.MovieURL == movieUrl }

    // Αν δεν υπάρχει ακόμα, ζήτα την ανάκτηση από το ViewModel
    LaunchedEffect(movieUrl) {
        viewModel.fetchDetailedMovieInfo(movieUrl)
    }

    val title = movie?.basicInfo?.title ?: "Φόρτωση..."

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
            // Movie Poster
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
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
                        Text(
                            text = "Poster της ταινίας",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Movie Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Movie Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Πληροφορίες Ταινίας",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (movie != null) {
                        // Basic Information
                        Text(
                            text = "Τίτλος: ${movie.basicInfo.title}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (movie.basicInfo.MovieURL.isNotEmpty()) {
                            Text(
                                text = "Σελίδα ταινίας: ${movie.basicInfo.MovieURL}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Detailed Information (if available)
                        if (movie.nowPlayingInfo != null) {
                            val info = movie.nowPlayingInfo
                            Text(
                                text = "Διάρκεια: ${info.duration}\n" +
                                        "Είδος: ${info.genre}\n" +
                                        "Ηλικιακός περιορισμός: ${info.ageRating}\n" +
                                        "Αίθουσα: ${info.projectionRoom}\n" +
                                        "Σκηνοθέτης: ${info.director}\n" +
                                        "Περιγραφή: ${info.description}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (info.cast.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ηθοποιοί: ${info.cast.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            if (info.showtime.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ώρες προβολής: ${info.showtime.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else if (movie.comingSoonInfo != null) {
                            val info = movie.comingSoonInfo
                            Text(
                                text = "Διάρκεια: ${info.duration}\n" +
                                        "Είδος: ${info.genre}\n" +
                                        "Ηλικιακός περιορισμός: ${info.ageRating}\n" +
                                        "Σκηνοθέτης: ${info.director}\n" +
                                        "Ημερομηνία πρεμιέρας: ${info.premiereDate}\n" +
                                        "Περιγραφή: ${info.description}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (info.cast.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ηθοποιοί: ${info.cast.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            Text(
                                text = "Δεν υπάρχουν επιπλέον πληροφορίες για αυτή την ταινία.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Text(
                            text = "Δεν βρέθηκε η ταινία.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* Add to favorites */ }
                ) {
                    Text("Αγαπημένα")
                }

                Button(
                    onClick = { /* Share movie */ }
                ) {
                    Text("Κοινοποίηση")
                }
            }
        }
    }
}
