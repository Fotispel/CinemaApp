package com.example.cinemaapp

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.cinemaapp.ui.screens.NowPlayingScreen
import com.example.cinemaapp.ui.screens.ComingSoonScreen
import com.example.cinemaapp.ui.screens.MoviePage
import com.example.cinemaapp.ui.theme.CinemaAppTheme
import com.example.cinemaapp.viewmodel.MovieViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Data class για τα Bottom Navigation Items
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

class MainActivity : ComponentActivity() {

    private val movieViewModel: MovieViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContent {
            CinemaAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    BottomNavigationItem(
                        title = "Προβάλλονται",
                        selectedIcon = ImageVector.vectorResource(id = R.drawable.baseline_theaters_24),
                        unselectedIcon = ImageVector.vectorResource(id = R.drawable.outline_theaters_24),
                        route = "now_playing"
                    ),
                    BottomNavigationItem(
                        title = "Προσεχώς",
                        selectedIcon = ImageVector.vectorResource(id = R.drawable.baseline_calendar_today_24),
                        unselectedIcon = ImageVector.vectorResource(id = R.drawable.outline_calendar_today_24),
                        route = "coming_soon"
                    )
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            if (currentRoute?.startsWith("movie_page") != true) {
                                CinemaTopBar(movieViewModel)
                            }
                        },
                        bottomBar = {
                            if (currentRoute?.startsWith("movie_page") != true) {
                                NavigationBar {
                                    items.forEachIndexed { index, item ->
                                        val isSelected = currentRoute == item.route
                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            },
                                            label = { Text(item.title) },
                                            selected = isSelected,
                                            onClick = {
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
                        }
                    ) { innerPadding ->
                        AnimatedNavHost(
                            navController = navController,
                            startDestination = "now_playing",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            composable(
                                route = "now_playing",
                                enterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(300)
                                    )
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(300)
                                    )
                                }
                            ) {
                                NowPlayingScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    viewModel = movieViewModel,
                                    navController = navController
                                )
                            }

                            composable(
                                route = "coming_soon",
                                enterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(300)
                                    )
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(300)
                                    )
                                }
                            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaTopBar(movieViewModel: MovieViewModel) {
    val cinemaDisplayMap = mapOf(
        "Texnopolis" to "Τεχνόπολις",
        "Pantelis" to "Παντελής",
        "Odeon" to "Odeon"
    )

    var expanded by rememberSaveable { mutableStateOf(false) }

    val selectedCinema by movieViewModel.selectedCinema.collectAsState()
    val currentCinemaDisplay = cinemaDisplayMap[selectedCinema] ?: selectedCinema

    val ubuntuMedium = FontFamily(Font(R.font.ubuntu_medium, weight = FontWeight.W500))

    TopAppBar(
        title = {
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Cinema Choices",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = currentCinemaDisplay,
                        fontFamily = ubuntuMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Odeon") },
                        onClick = {
                            expanded = false
                            movieViewModel.selectCinema("Odeon")
                            movieViewModel.fetchAllMovies("Odeon")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Τεχνόπολις") },
                        onClick = {
                            expanded = false
                            movieViewModel.selectCinema("Texnopolis")
                            movieViewModel.fetchAllMovies("Texnopolis")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Παντελής") },
                        onClick = {
                            expanded = false
                            movieViewModel.selectCinema("Pantelis")
                            movieViewModel.fetchAllMovies("Pantelis")
                        }
                    )
                }
            }
        }
    )
}
