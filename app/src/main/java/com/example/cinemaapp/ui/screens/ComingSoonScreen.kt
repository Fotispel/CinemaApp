package com.example.cinemaapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ComingSoonScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Ταινίες που έρχονται",
        modifier = modifier.padding(16.dp)
    )
}
