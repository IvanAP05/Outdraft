package com.example.outdraft.ui.pages.searchplayer

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.outdraft.R
import com.example.outdraft.ui.pages.searchplayer.playerdata.PlayerActivity

@Composable
fun SearchPlayerPage(activity: Activity) {
    val viewModel: SearchPlayerViewModel = viewModel()

    val state = viewModel.state.collectAsStateWithLifecycle()
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
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF455265),
                unfocusedContainerColor = Color(0xFF455265),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.Gray,
                focusedIndicatorColor = Color.Cyan,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (riotIDInput.isNotBlank()) {
                val parts = riotIDInput.split("#")
                if (parts.size == 2) {
                    val gameName = parts[0]
                    val tagLine = parts[1]

                    viewModel.getProfileIconAndLevel(activity, gameName, tagLine) { iconId, level, gameNameOf, tagLineOf, skinName->
                        PlayerActivity.start(activity, iconId, level, gameNameOf, tagLineOf, skinName)
                    }
                }
            }
        }, colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF455265),
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text("Obtener información del invocador")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.value.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.onBackground,
                strokeWidth = 4.dp
            )
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