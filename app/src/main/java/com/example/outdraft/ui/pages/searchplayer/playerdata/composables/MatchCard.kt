package com.example.outdraft.ui.pages.searchplayer.playerdata.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.outdraft.api.data.MatchSummary
import com.example.outdraft.common.RIOT_VERSION
import com.example.outdraft.ui.pages.searchplayer.playerdata.PlayerViewModel

@SuppressLint("DefaultLocale")
@Composable
fun MatchCard(
    match: MatchSummary,
    queueName: String = "",
    viewModel: PlayerViewModel,
    onClick: (String) -> Unit = {}
) {
    val victoriaColor = Color(0xFF1E88E5)
    val derrotaColor = Color(0xFFE53935)

    val borderColor = if (match.isWin) victoriaColor else derrotaColor
    val textColor = Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(match.matchId) },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(130.dp)
                        .background(color = borderColor)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/img/champion/${match.playerStats.championName}.png",
                        contentDescription = "Champion icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (match.isWin) "Victoria" else "Derrota",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = queueName.ifEmpty { match.gameMode },
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                        )

                        Text(
                            text = "Duración: ${match.gameDuration}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                        )
                        Text(
                            text = viewModel.getTimeAgo(match.gameCreation),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Text(
                            text = match.playerStats.championName,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${match.playerStats.kills}/${match.playerStats.deaths}/${match.playerStats.assists}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        val kda = if (match.playerStats.deaths == 0) {
                            "Perfect KDA"
                        } else {
                            String.format(
                                "%.2f:1 KDA",
                                (match.playerStats.kills + match.playerStats.assists).toFloat() / match.playerStats.deaths
                            )
                        }

                        Text(
                            text = kda,
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                        )

                        Text(
                            text = "${match.playerStats.totalMinionsKilled + match.playerStats.neutralMinionsKilled} CS",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }
    }
}