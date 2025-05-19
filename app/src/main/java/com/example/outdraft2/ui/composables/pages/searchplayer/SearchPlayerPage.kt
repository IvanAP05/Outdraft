package com.example.outdraft2.ui.composables.pages.searchplayer

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.outdraft2.R
import com.example.outdraft2.ui.composables.pages.searchplayer.playerdata.PlayerActivity

@Composable
fun SearchPlayerPage(activity: Activity) {
    val viewModel: SearchPlayerViewModel = viewModel()

    val isLoading = viewModel.isLoading.value
    val kdaText = viewModel.kda.value

    var riotIDInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = riotIDInput,
            onValueChange = { riotIDInput = it },
            label = { Text("Ingresa tu RiotID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (riotIDInput.isNotBlank()) {
                val parts = riotIDInput.split("#")
                if (parts.size == 2) {
                    val gameName = parts[0]
                    val tagLine = parts[1]

                    // Llamar al viewModel para cargar los datos
                    viewModel.getProfileIconAndLevel(gameName, tagLine) { iconId, level, gameNameOf, tagLineOf->
                        PlayerActivity.start(activity, iconId, level, gameNameOf, tagLineOf)
                    }
                }
            }
        }) {
            Text("Obtener información del invocador")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = kdaText,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Image(
            painter = painterResource(R.drawable.penguin_lol),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)


        )
    }
}