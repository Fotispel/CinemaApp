package com.example.cinemaapp.ui

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
import coil.compose.AsyncImage
import com.example.cinemaapp.data.MovieBasicInfo
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.request.ImageRequest

@Composable
fun MovieQuickViewItem(
    basicInfo: MovieBasicInfo,
    navController: NavController,
    onClick: (MovieBasicInfo) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
            ) {
                onClick(basicInfo)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (basicInfo.posterUrl.isNotEmpty()) {
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val context = LocalContext.current


            val screenWidthDp = configuration.screenWidthDp
            val gridContentPaddingDp = 16
            val gridSpacingsDp = 16
            val perItemOuterPaddingDp = 8
            val cellWidthDp = ((screenWidthDp - gridContentPaddingDp - gridSpacingsDp) / 3f) - (perItemOuterPaddingDp)
            val cellHeightDp = cellWidthDp * (3f / 2f)

            val (reqWidthPx, reqHeightPx) = with(density) {
                Pair((cellWidthDp.dp.toPx()).toInt().coerceAtLeast(1), (cellHeightDp.dp.toPx()).toInt().coerceAtLeast(1))
            }

            val imageRequest = ImageRequest.Builder(context)
                .data(basicInfo.posterUrl)
                .size(reqWidthPx, reqHeightPx)
                .crossfade(true)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = basicInfo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No Poster")
            }
        }
    }
}
