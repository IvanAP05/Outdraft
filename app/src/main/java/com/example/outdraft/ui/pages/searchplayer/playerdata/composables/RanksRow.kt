package com.example.outdraft.ui.pages.searchplayer.playerdata.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.outdraft.api.data.PlayerRank

@Composable
fun RanksRow(soloQRank: PlayerRank?, flexQRank: PlayerRank?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            RankDisplayCard(title = "Clasificatoria Solo/Dúo", rank = soloQRank)
        }
        Box(modifier = Modifier.weight(1f)) {
            RankDisplayCard(title = "Clasificatoria Flexible", rank = flexQRank)
        }
    }
}

