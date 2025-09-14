package com.example.cinemaapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cinemaapp.data.MovieBasicInfo
import com.example.cinemaapp.viewmodel.MovieViewModel

@Composable
fun NowPlayingScreen(viewModel: MovieViewModel, modifier: Modifier = Modifier) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
    }

    val movies = viewModel.movies.collectAsState().value

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(movies) { movie ->
            MovieQuickViewItem(movie.basicInfo)
        }
    }
}

@Composable
fun MovieQuickViewItem(basicInfo: MovieBasicInfo) {
    Column(modifier = Modifier.padding(4.dp)) {
        Image(
            painter = rememberAsyncImagePainter(basicInfo.posterUrl),
            contentDescription = basicInfo.title,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(text = basicInfo.title)
    }
}
