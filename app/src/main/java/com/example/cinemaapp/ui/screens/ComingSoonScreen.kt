package com.example.cinemaapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cinemaapp.viewmodel.MovieViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.cinemaapp.ui.MovieQuickViewItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(
    viewModel: MovieViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val comingSoonMovies = viewModel.comingSoonMovies.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value

    val pullToRefreshState = rememberPullToRefreshState()

    // Handle pull-to-refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refreshComingSoonMovies()
            pullToRefreshState.endRefresh()
        }
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
            items(
                comingSoonMovies,
                key = { movie ->
                    movie.basicInfo.posterUrl.ifEmpty { movie.basicInfo.title }
                },
                contentType = { _ -> "movie" }
            ) { movie ->
                MovieQuickViewItem(
                    basicInfo = movie.basicInfo,
                    navController = navController,
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
    }
}