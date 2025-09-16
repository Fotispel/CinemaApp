package com.example.cinemaapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cinemaapp.data.MovieBasicInfo
import android.net.Uri
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun MovieQuickViewItem(
    basicInfo: MovieBasicInfo,
    navController: NavController,
    onClick: (MovieBasicInfo) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val posterWidth = screenWidth / 2
    val posterHeight = posterWidth * 3 / 2

    Card(
        modifier = Modifier
            .width(posterWidth)
            .height(posterHeight)
            .padding(4.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
            ) {
                onClick(basicInfo)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (basicInfo.posterUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = basicInfo.posterUrl),
                    contentDescription = basicInfo.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(posterWidth)
                        .height(posterHeight)

                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("No Poster")
                }
            }
        }
    }
}
