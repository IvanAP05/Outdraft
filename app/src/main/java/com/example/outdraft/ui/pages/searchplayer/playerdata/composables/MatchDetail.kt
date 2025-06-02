package com.example.outdraft.ui.pages.searchplayer.playerdata.composables

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.outdraft.api.data.ChampionBan
import com.example.outdraft.api.data.MatchDetail
import com.example.outdraft.api.data.MatchParticipant
import com.example.outdraft.common.RIOT_VERSION
import com.example.outdraft.ui.utils.getChampionNameFromId

@Composable
fun MatchDetailBottomSheet(
    matchDetail: MatchDetail?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (matchDetail != null) {
            LazyColumn {
                item {
                    MatchDetailHeader(matchDetail)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    val team1Won = matchDetail.getTeam1().firstOrNull()?.win ?: false
                    TeamHeader(
                        teamName = "Equipo Azul",
                        teamColor = Color(0xFF1E88E5),
                        isWinner = team1Won
                    )
                }

                items(matchDetail.getTeam1().size) { index ->
                    ParticipantCard(matchDetail.getTeam1()[index])
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    val team1Bans = matchDetail.teams.find { it.teamId == 100 }?.bans ?: emptyList()
                    if (team1Bans.isNotEmpty()) {
                        BansSection(bans = team1Bans, context = context)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    HorizontalDivider(
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    val team2Won = matchDetail.getTeam2().firstOrNull()?.win ?: false
                    TeamHeader(
                        teamName = "Equipo Rojo",
                        teamColor = Color(0xFFE53935),
                        isWinner = team2Won
                    )
                }

                items(matchDetail.getTeam2().size) { index ->
                    ParticipantCard(matchDetail.getTeam2()[index])
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    val team2Bans = matchDetail.teams.find { it.teamId == 200 }?.bans ?: emptyList()
                    if (team2Bans.isNotEmpty()) {
                        BansSection(bans = team2Bans, context = context)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun MatchDetailHeader(matchDetail: MatchDetail) {
    Column {
        if (matchDetail.gameMode != "CLASSIC") {
            Text(
                text = matchDetail.gameMode,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(
            text = "Duración: ${matchDetail.gameDuration}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TeamHeader(
    teamName: String,
    teamColor: Color,
    isWinner: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleMedium.copy(
                color = teamColor,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = if (isWinner) "VICTORIA" else "DERROTA",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isWinner) Color(0xFF4CAF50) else Color(0xFFE53935),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ParticipantCard(participant: MatchParticipant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/img/champion/${participant.championName}.png",
                    contentDescription = "Champion",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (participant.riotIdGameName.isNotEmpty()) {
                            "${participant.riotIdGameName}#${participant.riotIdTagline}"
                        } else {
                            participant.summonerName.ifEmpty { "Jugador" }
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${participant.kills}/${participant.deaths}/${participant.assists}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val kda = if (participant.deaths == 0) {
                        "Perfect"
                    } else {
                        String.format("%.1f", (participant.kills + participant.assists).toFloat() / participant.deaths)
                    }

                    Text(
                        text = "$kda KDA",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemsBuild(participant)

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${participant.totalMinionsKilled + participant.neutralMinionsKilled} CS",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )

                    Text(
                        text = "${participant.goldEarned / 1000}k oro",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )

                    Text(
                        text = "${participant.totalDamageDealtToChampions / 1000}k daño",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemsBuild(participant: MatchParticipant) {
    val items = listOf(
        participant.item0,
        participant.item1,
        participant.item2,
        participant.item3,
        participant.item4,
        participant.item5
    ).filter { it != 0 }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(items.size) { index ->
            AsyncImage(
                model = "https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/img/item/${items[index]}.png",
                contentDescription = "Item",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        items(6 - items.size) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun BansSection(bans: List<ChampionBan>, context: Context) {
    Column {
        Text(
            text = "Bans:",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bans.size) { index ->
                val ban = bans[index]
                if (ban.championId != -1) {
                    val championName = getChampionNameFromId(context, ban.championId.toString() )
                    AsyncImage(
                        model = "https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/img/champion/${championName}.png",
                        contentDescription = "Banned champion",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}