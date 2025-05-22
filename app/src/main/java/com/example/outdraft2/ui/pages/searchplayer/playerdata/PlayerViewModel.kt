package com.example.outdraft2.ui.pages.searchplayer.playerdata

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outdraft2.api.RiotApiInstance
import com.example.outdraft2.api.data.MatchDetail
import com.example.outdraft2.api.data.MatchSummary
import com.example.outdraft2.api.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val summonerName: String,
    private val tagLine: String
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var playerPuuid: String = ""
    private val matchesPerLoad = 10
    private var startIndex = 0
    private val apiKey = "RGAPI-b2883465-e745-4c1e-a30c-a15de3a49c76"

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val account = RiotApiInstance.apiRiot.getAccountByRiotId(summonerName, tagLine, apiKey)
                playerPuuid = account.puuid

                launch { loadPlayerRank() }
                launch { loadMatchBatch() }

            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al obtener datos del invocador", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    private fun loadPlayerRank() {
        viewModelScope.launch {
            try {
                val rankList = RiotApiInstance.apiRiot.getRankByPUUID(playerPuuid, apiKey)

                val soloQRank = rankList.find { it.queueType == "RANKED_SOLO_5x5" }
                val flexRank = rankList.find { it.queueType == "RANKED_FLEX_SR" }

                _state.update { state ->
                    state.copy(
                        soloQueueRank = soloQRank,
                        flexQueueRank = flexRank
                    )
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al obtener el rango del jugador", e)
            }
        }
    }

    fun loadMoreMatches() {
        if (_state.value.isLoadingMore) return
        startIndex += matchesPerLoad
        loadMatchBatch()
    }

    @SuppressLint("DefaultLocale")
    private fun loadMatchBatch() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            try {
                val matchIds = RiotApiInstance.apiRiot.getMatchesListByPUUID(
                    puuid = playerPuuid,
                    apiKey = apiKey,
                    start = startIndex,
                    count = matchesPerLoad
                )

                if (matchIds.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            hasMoreMatches = false
                        )
                    }
                    return@launch
                }

                val newMatchSummaries = mutableListOf<MatchSummary>()

                for (matchId in matchIds) {
                    try {
                        val matchInfo = RiotApiInstance.apiRiot.getMatchInfoById(matchId, apiKey)
                        val participantIndex = matchInfo.metadata.participants.indexOf(playerPuuid)

                        if (participantIndex != -1) {
                            val playerStats = matchInfo.info.participants[participantIndex]
                            val duration = formatGameDuration(matchInfo.info.gameDuration)

                            newMatchSummaries.add(
                                MatchSummary(
                                    matchId = matchId,
                                    gameDuration = duration,
                                    gameType = matchInfo.info.gameType,
                                    queueId = matchInfo.info.queueId,
                                    gameMode = matchInfo.info.gameMode,
                                    playerStats = playerStats,
                                    isWin = playerStats.win,
                                    gameCreation = matchInfo.info.gameCreation
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("PlayerViewModel", "Error fetching match $matchId: ${e.message}")
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        matchSummaries = it.matchSummaries + newMatchSummaries,
                        hasMoreMatches = matchIds.size == matchesPerLoad
                    )
                }

            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al cargar partidas", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun loadMatchDetail(matchId: String) {
        if (_state.value.isLoadingMatchDetail) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMatchDetail = true) }

            try {
                val matchInfo = RiotApiInstance.apiRiot.getMatchInfoById(matchId, apiKey)
                val duration = formatGameDuration(matchInfo.info.gameDuration)

                val matchDetail = MatchDetail(
                    matchId = matchId,
                    gameDuration = duration,
                    gameType = matchInfo.info.gameType,
                    gameMode = matchInfo.info.gameMode,
                    queueId = matchInfo.info.queueId,
                    gameCreation = matchInfo.info.gameCreation,
                    participants = matchInfo.info.participants,
                    teams = matchInfo.info.teams
                )

                _state.update {
                    it.copy(
                        selectedMatchDetail = matchDetail,
                        isLoadingMatchDetail = false
                    )
                }

            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error loading match detail: ${e.message}")
                _state.update {
                    it.copy(
                        isLoadingMatchDetail = false,
                        error = e.message ?: "Error al cargar detalles de la partida"
                    )
                }
            }
        }
    }

    fun clearMatchDetail() {
        _state.update { it.copy(selectedMatchDetail = null) }
    }

    @SuppressLint("DefaultLocale")
    private fun formatGameDuration(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    fun getQueueName(queueId: Int): String {
        return when (queueId) {
            420 -> "Ranked Solo/Duo"
            440 -> "Ranked Flex"
            450 -> "ARAM"
            400 -> "Normal Draft"
            430 -> "Normal Blind"
            700 -> "Clash"
            else -> "Partida personalizada"
        }
    }
}