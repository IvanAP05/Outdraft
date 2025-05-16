package com.example.outdraft2.ui.composables.pages.searchplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import com.example.outdraft2.api.RetrofitInstance

class SearchPlayerViewModel : ViewModel() {
    val kda = mutableStateOf("¡Busca el invocador que quieras!")

    val isLoading = mutableStateOf(false)

    fun fetchKDAForPlayer(riotId: String) {
        val apiKey = "RGAPI-b2883465-e745-4c1e-a30c-a15de3a49c76"

        viewModelScope.launch {
            isLoading.value = true
            try {
                val (gameName, tagLine) = riotId.split("#")
                val account = RetrofitInstance.api.getAccountByRiotId(gameName, tagLine, apiKey)
                val puuid = account.puuid

                val matchIds = RetrofitInstance.api.getMatchIdsByPUUID(puuid, apiKey)
                if (matchIds.isEmpty()) {
                    kda.value = "No se encontraron partidas recientes"
                    return@launch
                }

                var totalKills = 0
                var totalDeaths = 0
                var totalAssists = 0

                for (matchId in matchIds) {
                    val matchDetails = RetrofitInstance.api.getMatchDetails(matchId, apiKey)
                    val playerData = matchDetails.info.participants.find { it.puuid == puuid }
                    playerData?.let {
                        totalKills += it.kills
                        totalDeaths += it.deaths
                        totalAssists += it.assists
                    }
                }

                val matchesCounted = matchIds.size
                val avgKills = totalKills.toFloat() / matchesCounted
                val avgDeaths = totalDeaths.toFloat() / matchesCounted
                val avgAssists = totalAssists.toFloat() / matchesCounted

                val avgKDA = String.format("%.1f/%.1f/%.1f", avgKills, avgDeaths, avgAssists)
                kda.value = "KDA promedio (últimas $matchesCounted partidas): $avgKDA"

            } catch (e: Exception) {
                kda.value = "Error: ${e.message}"
            }
            finally {
                isLoading.value = false
            }
        }
    }}