package com.example.outdraft2.ui.pages.searchplayer.playerdata.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun TopBar(
    imageUrl: String,
    profileIconUrl: String,
    summonerLevel: Int,
    summonerName: String,
    tagLine: String,
    onBackClick: () -> Unit,
    collapseFraction: Float = 0f
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "TopBar Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f - collapseFraction
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = (0.6f * (1f - collapseFraction))),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 500f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = collapseFraction * 0.8f)
                )
        )

        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
            contentDescription = "Volver",
            tint = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
                .graphicsLayer {
                    alpha = 1f - (collapseFraction * 1.5f).coerceIn(0f, 1f)
                }
                .clickable {
                    onBackClick()
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(
                        if (collapseFraction < 0.5f) Alignment.BottomStart else Alignment.CenterStart
                    )
                    .graphicsLayer {
                        val scale = 1f - (collapseFraction * 0.3f)
                        scaleX = scale
                        scaleY = scale

                        translationY = -collapseFraction * 20f
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = profileIconUrl,
                        contentDescription = "User icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(
                                (72 - (collapseFraction * 32)).dp
                            )
                            .clip(CircleShape)
                    )

                    AnimatedVisibility(
                        visible = collapseFraction < 0.5f,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Nivel: $summonerLevel",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "$summonerName #$tagLine",
                        style = TextStyle(
                            fontSize = (28 - (collapseFraction * 12)).sp,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}