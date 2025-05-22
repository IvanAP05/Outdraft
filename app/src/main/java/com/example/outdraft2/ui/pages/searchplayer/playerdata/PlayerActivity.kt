package com.example.outdraft2.ui.pages.searchplayer.playerdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.outdraft2.R
import com.example.outdraft2.ui.pages.searchplayer.playerdata.composables.PlayerScreen
import com.example.outdraft2.ui.theme.Outdraft2Theme

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileIconId = intent.getIntExtra("profileIconId", 0)
        val summonerLevel = intent.getIntExtra("summonerLevel", 0)
        val summonerName = intent.getStringExtra("summonerName") ?: "Invocador"
        val tagLine = intent.getStringExtra("tagLine") ?: "0000"
        val skinName = intent.getStringExtra("skinName") ?: "0000"

        enableEdgeToEdge()

        setContent {
            val viewModel: PlayerViewModel = viewModel(
                factory = PlayerViewModelFactory(summonerName, tagLine)
            )

            val playerState by viewModel.state.collectAsState()
            val context = LocalContext.current

            Outdraft2Theme {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.background_outdraft),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(modifier = Modifier.padding(innerPadding)) {
                            PlayerScreen(
                                profileIconId = profileIconId,
                                summonerLevel = summonerLevel,
                                summonerName = summonerName,
                                tagLine = tagLine,
                                skinName = skinName,
                                playerState = playerState,
                                viewModel = viewModel,
                                onBackClick = { finish() },
                                context = context
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun start(activity: Activity, profileIconId: Int, summonerLevel: Int, summonerName: String, tagLine: String, skinName: String) {
            val intent = Intent(activity, PlayerActivity::class.java).apply {
                putExtra("profileIconId", profileIconId)
                putExtra("summonerLevel", summonerLevel)
                putExtra("summonerName", summonerName)
                putExtra("tagLine", tagLine)
                putExtra("skinName", skinName)
            }
            activity.startActivity(intent)
        }
    }
}