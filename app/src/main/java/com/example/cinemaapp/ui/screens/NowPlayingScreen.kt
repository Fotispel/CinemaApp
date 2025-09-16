package com.example.cinemaapp.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.cinemaapp.viewmodel.MovieViewModel
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: MovieViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val movies = viewModel.movies.collectAsState().value

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        pullToRefreshState.startRefresh()
        viewModel.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
        pullToRefreshState.endRefresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
                .fillMaxSize()
        ) {

            items(movies) { movie ->
                MovieQuickViewItem(
                    basicInfo = movie.basicInfo,
                    navController = navController, // μπορεί να αφαιρεθεί αν δεν χρειάζεται
                    onClick = { clickedMovie ->
                        val encodedUrl = Uri.encode(clickedMovie.MovieURL)
                        navController.navigate("movie_page/$encodedUrl")
                    }
                )
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (pullToRefreshState.isRefreshing) {
            LaunchedEffect(true) {
                viewModel.fetchMovies("https://cinelandpantelis.gr/proballontai.html")
                pullToRefreshState.endRefresh()
            }
        }
    }
}

