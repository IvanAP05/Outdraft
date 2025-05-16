package com.example.outdraft2.ui.composables.pages.searchplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.outdraft2.R

@Composable
fun SearchPlayerPage() {
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
                viewModel.fetchKDAForPlayer(riotIDInput)
            }
        }) {
            Text("Obtener KDA")
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
