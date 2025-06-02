package com.example.outdraft.ui.pages.searchplayer.playerdata.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.outdraft.R
import com.example.outdraft.api.data.PlayerRank

@SuppressLint("DefaultLocale")
@Composable
fun RankDisplayCard(title: String, rank: PlayerRank?) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (rank == null) {

                Text(
                    text = "Sin clasificar",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.padding(8.dp)
                )

            } else {
                Image(
                    painter = when (rank.tier) {
                        "IRON" -> painterResource(id = R.drawable.iron)
                        "BRONZE" -> painterResource(id = R.drawable.bronze)
                        "SILVER" -> painterResource(id = R.drawable.silver)
                        "GOLD" -> painterResource(id = R.drawable.gold)
                        "PLATINUM" -> painterResource(id = R.drawable.platinum)
                        "DIAMOND" -> painterResource(id = R.drawable.diamond)
                        "MASTER" -> painterResource(id = R.drawable.master)
                        "GRANDMASTER" -> painterResource(id = R.drawable.grandmaster)
                        "CHALLENGER" -> painterResource(id = R.drawable.challenger)
                        else -> painterResource(id = R.drawable.iron)
                    },
                    contentDescription = "Rango ${rank.tier} ${rank.rank}",
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = rank.tier,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "${rank.rank} (${rank.leaguePoints} LP)",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )

                Text(
                    text = "${rank.wins}W/${rank.losses}L (${
                        String.format(
                            "%.1f%%",
                            (rank.wins.toFloat() / (rank.wins + rank.losses)) * 100
                        )
                    })",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}