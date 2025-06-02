package com.example.outdraft.ui.pages.searchplayer.playerdata.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.outdraft.R
import com.example.outdraft.api.data.PlayerRank

@Composable
fun RankDisplay(rank: PlayerRank?) {
    if (rank == null) {
        Text(
            text = "Sin clasificar",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "${rank.tier} ${rank.rank} (${rank.leaguePoints} LP)",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
        }
    }
}