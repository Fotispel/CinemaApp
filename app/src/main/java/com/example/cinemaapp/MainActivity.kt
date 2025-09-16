package com.example.cinemaapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cinemaapp.ui.NowPlayingScreen
import com.example.cinemaapp.ui.screens.ComingSoonScreen
import com.example.cinemaapp.ui.screens.MoviePage
import com.example.cinemaapp.ui.theme.CinemaAppTheme
import com.example.cinemaapp.viewmodel.MovieViewModel

// Data class για τα Bottom Navigation Items
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

class MainActivity : ComponentActivity() {

    private val movieViewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinemaAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    BottomNavigationItem(
                        title = "Προβάλλονται",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        route = "now_playing"
                    ),
                    BottomNavigationItem(
                        title = "Προσεχώς",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        route = "coming_soon"
                    )
                )

                var selectedItem by rememberSaveable { mutableIntStateOf(0) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentRoute?.startsWith("movie_page") == true) {
                        // FULL SCREEN χωρίς top/bottom bar
                        NavHost(
                            navController = navController,
                            startDestination = "now_playing",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("now_playing") {
                                NowPlayingScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    viewModel = movieViewModel,
                                    navController = navController
                                )
                            }
                            composable("coming_soon") {
                                ComingSoonScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    viewModel = movieViewModel,
                                    navController = navController
                                )
                            }

                            composable("movie_page/{movieUrl}") { backStackEntry ->
                                val movieUrl = backStackEntry.arguments?.getString("movieUrl")
                                    ?.let { Uri.decode(it) } ?: ""
                                MoviePage(
                                    movieUrl = movieUrl,
                                    navController = navController,
                                    viewModel = movieViewModel
                                )
                            }
                        }
                    } else {
                        // Κανονικό Scaffold με TopBar & BottomBar
                        Scaffold(
                            topBar = { CinemaTopBar() },
                            bottomBar = {
                                NavigationBar {
                                    items.forEachIndexed { index, item ->
                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    imageVector = if (selectedItem == index) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            },
                                            label = { Text(item.title) },
                                            selected = selectedItem == index,
                                            onClick = {
                                                selectedItem = index
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = "now_playing",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding) // Χρήση του innerPadding
                            ) {
                                composable("now_playing") {
                                    NowPlayingScreen(
                                        modifier = Modifier.fillMaxSize(),
                                        viewModel = movieViewModel,
                                        navController = navController
                                    )
                                }
                                composable("coming_soon") {
                                    ComingSoonScreen(
                                        modifier = Modifier.fillMaxSize(),
                                        viewModel = movieViewModel,
                                        navController = navController
                                    )
                                }

                                composable("movie_page/{movieUrl}") { backStackEntry ->
                                    val movieUrl = backStackEntry.arguments?.getString("movieUrl")
                                        ?.let { Uri.decode(it) } ?: ""
                                    MoviePage(
                                        movieUrl = movieUrl,
                                        navController = navController,
                                        viewModel = movieViewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaTopBar() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var currentCinema by rememberSaveable { mutableStateOf("Παντελής") }

    TopAppBar(
        title = {
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Cinema Choices"
                        )
                    }
                    Text(
                        text = currentCinema,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Παντελής") },
                        onClick = {
                            currentCinema = "Παντελής"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Τεχνόπολις") },
                        onClick = {
                            currentCinema = "Τεχνόπολις"
                            expanded = false
                        }
                    )
                }
            }
        }
    )
}
