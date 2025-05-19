package com.example.outdraft2.ui.composables.pages.searchplayer.playerdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.outdraft2.ui.theme.Outdraft2Theme

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileIconId = intent.getIntExtra("profileIconId", 0)
        val summonerLevel = intent.getIntExtra("summonerLevel", 0)
        val summonerName = intent.getStringExtra("summonerName") ?: "Invocador"
        val tagLine = intent.getStringExtra("tagLine") ?: "0000"

        enableEdgeToEdge()
        setContent {
            Outdraft2Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PlayerScreen(profileIconId, summonerLevel, summonerName, tagLine)
                    }
                }
            }
        }
    }

    @Composable
    fun TopBar(
        imageUrl: String,
        profileIconUrl: String,
        summonerLevel: Int,
        summonerName: String,
        tagLine: String,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Fondo
            AsyncImage(
                model = imageUrl,
                contentDescription = "TopBar Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
                            startY = 0f,
                            endY = 500f
                        )
                    )
            )

            // Flecha volver
            Icon(
                imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                       finish()
                    }
            )

            // Contenido inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = profileIconUrl,
                        contentDescription = "User icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "Nivel: $summonerLevel",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    text = "$summonerName #$tagLine" ,
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }




    @Composable
    fun PlayerScreen(profileIconId: Int, summonerLevel: Int, summonerName: String, tagLine: String) {
        val profileIconUrl = "https://ddragon.leagueoflegends.com/cdn/15.10.1/img/profileicon/$profileIconId.png"

        Box(modifier = Modifier.fillMaxSize()) {
            TopBar(
                imageUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Ezreal_9.jpg",
                profileIconUrl = profileIconUrl,
                summonerLevel = summonerLevel,
                summonerName = summonerName,
                tagLine = tagLine
            )
        }
    }

    companion object {
        fun start(activity: Activity, profileIconId: Int, summonerLevel: Int, summonerName: String, tagLine: String) {
            val intent = Intent(activity, PlayerActivity::class.java).apply {
                putExtra("profileIconId", profileIconId)
                putExtra("summonerLevel", summonerLevel)
                putExtra("summonerName", summonerName)
                putExtra("tagLine", tagLine)
            }
            activity.startActivity(intent)
        }
    }
}



