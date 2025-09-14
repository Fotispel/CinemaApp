package com.example.cinemaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.cinemaapp.ui.NowPlayingScreen
import com.example.cinemaapp.ui.theme.CinemaAppTheme
import com.example.cinemaapp.ui.screens.ComingSoonScreen
import com.example.cinemaapp.viewmodel.MovieViewModel

// Data class για τα Bottom Navigation Items
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

class MainActivity : ComponentActivity() {

    // Δημιουργία ViewModel
    private val movieViewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinemaAppTheme {

                val items = listOf(
                    BottomNavigationItem(
                        title = "Προβάλλονται",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home
                    ),
                    BottomNavigationItem(
                        title = "Προσεχώς",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home
                    )
                )

                var selectedItem by rememberSaveable { mutableIntStateOf(0) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = { CinemaTopBar() },
                        bottomBar = {
                            NavigationBar {
                                items.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(imageVector = item.selectedIcon, contentDescription = item.title) },
                                        label = { Text(item.title) },
                                        selected = items[selectedItem] == item,
                                        onClick = { selectedItem = items.indexOf(item) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        when (selectedItem) {
                            0 -> NowPlayingScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = movieViewModel
                            )
                            1 -> ComingSoonScreen(Modifier.padding(innerPadding))
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
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Cinema Choices"
                        )
                    }
                    Text(
                        text = currentCinema,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
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
