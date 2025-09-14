package com.example.cinemaapp.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun MoviePage(title: String) {
    Text(text = "Πληροφορίες για: $title")
}
