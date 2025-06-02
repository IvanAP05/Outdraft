package com.example.outdraft.ui.pages.searchplayer.playerdata.composables

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.outdraft.api.data.PlayerState
import com.example.outdraft.common.RIOT_VERSION
import com.example.outdraft.ui.pages.searchplayer.playerdata.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    profileIconId: Int,
    summonerLevel: Int,
    summonerName: String,
    tagLine: String,
    skinName: String,
    playerState: PlayerState,
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    context: Context
) {
    val profileIconUrl = "https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/img/profileicon/$profileIconId.png"
    val splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/$skinName.jpg"

    val scrollState = rememberLazyListState()
    val collapseThreshold = 150f

    val collapseFraction by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) {
                1f
            } else {
                val scrollOffset = scrollState.firstVisibleItemScrollOffset.toFloat()
                (scrollOffset / collapseThreshold).coerceIn(0f, 1f)
            }
        }
    }

    val currentHeight by animateDpAsState(
        targetValue = (200 - (collapseFraction * 144)).dp,
        animationSpec = tween(durationMillis = 0),
        label = "TopBarHeight"
    )

    val showFloatingButton by remember {
        derivedStateOf {
            collapseFraction > 0.7f
        }
    }

    var previousScrollOffset by remember { mutableFloatStateOf(0f) }
    var isScrollingUp by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val currentScrollOffset = index * 1000f + offset
            isScrollingUp = currentScrollOffset < previousScrollOffset
            previousScrollOffset = currentScrollOffset
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(playerState.selectedMatchDetail) {
        showBottomSheet = playerState.selectedMatchDetail != null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentHeight)
                        .clipToBounds()
                ) {
                    TopBar(
                        imageUrl = splashUrl,
                        profileIconUrl = profileIconUrl,
                        summonerLevel = summonerLevel,
                        summonerName = summonerName,
                        tagLine = tagLine,
                        onBackClick = onBackClick,
                        collapseFraction = collapseFraction
                    )
                }
            }

            item {
                RanksRow(
                    soloQRank = playerState.soloQueueRank,
                    flexQRank = playerState.flexQueueRank
                )
            }

            if (playerState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
            else if (playerState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${playerState.error}",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Red)
                        )
                    }
                }
            }
            else {
                if (playerState.matchSummaries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron partidas recientes",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Historial de partidas",
                            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    items(playerState.matchSummaries.size) { index ->
                        val match = playerState.matchSummaries[index]
                        MatchCard(
                            match = match,
                            viewModel = viewModel,
                            queueName = viewModel.getQueueName(match.queueId),
                            onClick = { matchId ->
                                viewModel.loadMatchDetail(matchId)
                            }
                        )
                    }

                    item {
                        if (playerState.hasMoreMatches) {
                            if (playerState.isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.5f)
                                            .clickable {
                                                viewModel.loadMoreMatches()
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2B3A4B)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Ver más partidas",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay más partidas disponibles",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showFloatingButton,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            FloatingActionButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp),
                containerColor = Color.Black.copy(alpha = 0.7f),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                viewModel.clearMatchDetail()
            },
            sheetState = bottomSheetState,
            containerColor = Color(0xFF1A1A1A),
            contentColor = Color.White,
            dragHandle = null
        ) {
            MatchDetailBottomSheet(
                matchDetail = playerState.selectedMatchDetail,
                isLoading = playerState.isLoadingMatchDetail,
                onDismiss = {
                    showBottomSheet = false
                    viewModel.clearMatchDetail()
                },
                context = context
            )
        }
    }
}