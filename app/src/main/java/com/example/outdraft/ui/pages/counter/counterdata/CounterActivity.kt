package com.example.outdraft.ui.pages.counter.counterdata

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.outdraft.R
import com.example.outdraft.api.data.Champion
import com.example.outdraft.api.data.CounterUiState
import com.example.outdraft.api.data.WinRatio
import com.example.outdraft.ui.theme.BuildColors
import com.example.outdraft.ui.theme.OutdraftTheme

class CounterActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CHAMPION_ID = "champion_id"
        const val EXTRA_CHAMPION_NAME = "champion_name"
        const val EXTRA_CHAMPION_KEY = "champion_key"
    }

    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val championId = intent.getStringExtra(EXTRA_CHAMPION_KEY) ?: ""
        val championName = intent.getStringExtra(EXTRA_CHAMPION_NAME) ?: ""

        val selectedChampion = Champion(
            key = championId,
            name = championName,
            id = championId,
            tags = emptyList()
        )

        setContent {
            OutdraftTheme {
                CounterDataScreen(
                    champion = selectedChampion,
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}



@Composable
fun CounterDataScreen(
    champion: Champion,
    viewModel: CounterViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(champion.key) {
        viewModel.loadWinRatiosForChampion(champion.key)
    }

    Image(
        painter = painterResource(id = R.drawable.background_outdraft),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ChampionHeader(
            champion = champion,
            onBackClick = onBackClick
        )

        FilterSection(
            uiState = uiState,
            onRankSelected = { rank ->
                viewModel.updateSelectedRank(rank)
            },
            onPositionSelected = { position ->
                viewModel.updateSelectedPosition(position)
            },
            onSearchQueryChanged = { query ->
                viewModel.updateSearchQuery(query)
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            CounterContent(uiState = uiState)
        }
    }
}

@Composable
fun FilterSection(
    uiState: CounterUiState,
    onRankSelected: (String) -> Unit,
    onPositionSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    var rankExpanded by remember { mutableStateOf(false) }
    var positionExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BuildColors.FilterBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(
                        text = "Buscar campeón...",
                        color = BuildColors.LightBlue
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = BuildColors.MediumBlue.copy(alpha = 0.6f),
                    unfocusedContainerColor = BuildColors.MediumBlue.copy(alpha = 0.4f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = BuildColors.LightBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rankExpanded = true },
                        colors = CardDefaults.cardColors(
                            containerColor = BuildColors.MediumBlue.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedRank ?: "Todos los rangos",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = BuildColors.LightBlue
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = rankExpanded,
                        onDismissRequest = { rankExpanded = false },
                        modifier = Modifier.background(BuildColors.DarkBlue.copy(alpha = 0.95f))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Todos los rangos",
                                    color = Color.White,
                                    fontWeight = if (uiState.selectedRank == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onRankSelected("ALL")
                                rankExpanded = false
                            }
                        )

                        uiState.availableRanks.forEach { rank ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = rank,
                                        color = Color.White,
                                        fontWeight = if (uiState.selectedRank == rank) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onRankSelected(rank)
                                    rankExpanded = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { positionExpanded = true },
                        colors = CardDefaults.cardColors(
                            containerColor = BuildColors.MediumBlue.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedPosition ?: "Todos los roles",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = BuildColors.LightBlue
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = positionExpanded,
                        onDismissRequest = { positionExpanded = false },
                        modifier = Modifier.background(BuildColors.DarkBlue.copy(alpha = 0.95f))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Todos los roles",
                                    color = Color.White,
                                    fontWeight = if (uiState.selectedPosition == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onPositionSelected("ALL")
                                positionExpanded = false
                            }
                        )

                        uiState.availablePositions.forEach { position ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = position,
                                        color = Color.White,
                                        fontWeight = if (uiState.selectedPosition == position) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onPositionSelected(position)
                                    positionExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChampionHeader(
    champion: Champion,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(champion.imageSplashUrl)
                .memoryCacheKey(champion.key)
                .diskCacheKey(champion.key)
                .crossfade(true)
                .build(),
            contentDescription = "Header Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BuildColors.DarkBlue.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier
                    .background(
                        BuildColors.DarkBlue.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = champion.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (champion.tags.isNotEmpty()) {
                Text(
                    text = champion.tags.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BuildColors.LightBlue
                )
            }
        }
    }
}

@Composable
fun CounterContent(uiState: CounterUiState) {
    when {
        uiState.isLoading -> LoadingContent()
        uiState.errorMessage != null -> ErrorContent(uiState.errorMessage)
        uiState.filteredWinRatios.isEmpty() && uiState.allWinRatios.isNotEmpty() -> EmptyFilterContent()
        uiState.allWinRatios.isEmpty() -> EmptyFilterContent()
        else -> WinRatiosContent(uiState.filteredWinRatios)
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BuildColors.LightBlue)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando winratios...",
                color = Color.White
            )
        }
    }
}

@Composable
fun ErrorContent(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage,
            color = Color(0xFFFF5252),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EmptyFilterContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se encontraron matchups con los filtros aplicados.\nPrueba a cambiar los filtros o borrar la búsqueda.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = BuildColors.LightBlue
        )
    }
}

@Composable
fun WinRatiosContent(winRatios: List<WinRatio>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(winRatios) { winRatio ->
            WinRatioCard(winRatio = winRatio)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun WinRatioCard(winRatio: WinRatio) {
    val winRatePercentage = (winRatio.winRate / 100.0).coerceIn(0.0, 1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    BuildColors.CardBackground,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(winRatePercentage.toFloat())
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                BuildColors.WinRateGradient.copy(alpha = 0.7f),
                                BuildColors.AccentBlue.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(winRatio.enemyChampion.imageIconUrl)
                        .memoryCacheKey(winRatio.enemyChampion.key)
                        .diskCacheKey(winRatio.enemyChampion.key)
                        .crossfade(true)
                        .build(),
                    contentDescription = winRatio.enemyChampion.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = winRatio.enemyChampion.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${winRatio.gamesPlayed} partidas",
                        style = MaterialTheme.typography.bodySmall,
                        color = BuildColors.LightBlue
                    )
                }

                Text(
                    text = "${String.format("%.1f", winRatio.winRate)}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .background(
                            BuildColors.DarkBlue.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}