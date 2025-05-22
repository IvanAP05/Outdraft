package com.example.outdraft2.ui.pages.searchplayer

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outdraft2.api.RiotApiInstance
import com.example.outdraft2.api.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class SearchPlayerViewModel : ViewModel() {
    val kda = mutableStateOf("¡Busca el invocador que quieras!")
    private var _mutableState = MutableStateFlow(PlayerState())

    val state: StateFlow<PlayerState> = _mutableState

    fun getProfileIconAndLevel(
        context: Context,
        gameName: String,
        tagLine: String,
        onResult: (Int, Int, String, String, String) -> Unit
    ) {
        viewModelScope.launch {
            _mutableState.update {
                it.copy(isLoading = true)
            }
            try {
                val apiKey = "RGAPI-b2883465-e745-4c1e-a30c-a15de3a49c76"
                val account = RiotApiInstance.apiRiot.getAccountByRiotId(gameName, tagLine, apiKey)
                val info = RiotApiInstance.apiRiot.getAccountInfoByPUUID(account.puuid, apiKey)
                val topChampList = RiotApiInstance.apiRiot.getTopChampByPUUID(account.puuid, apiKey)

                if (topChampList.isNotEmpty()) {
                    val topChampionId = topChampList[0].championId.toString()
                    val champName = getChampionNameFromId(context, topChampionId)
                    val skinName = champName?.let { getRandomSkinFromChampion(it) }
                    if (skinName != null) {
                        onResult(
                            info.profileIconId,
                            info.summonerLevel,
                            account.gameName,
                            account.tagLine,
                            skinName,
                        )
                    }
                } else {
                    onResult(
                        info.profileIconId,
                        info.summonerLevel,
                        account.gameName,
                        account.tagLine,
                        "default_skin"
                    )
                }
            } catch (e: Exception) {
                Log.e("SearchPlayerVM", "Error al obtener datos del invocador", e)
            }
            finally {
                _mutableState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun readJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun getChampionNameFromId(context: Context, championId: String): String? {
        val json = readJsonFromAssets(context, "data/champion.json")
        val root = JSONObject(json)
        val data = root.getJSONObject("data")

        val keys = data.keys()
        for (key in keys) {
            val champ = data.getJSONObject(key)
            if (champ.getString("key") == championId) {
                return champ.getString("id")
            }
        }

        return null
    }

    private suspend fun getRandomSkinFromChampion(championName: String): String {
        val response = RiotApiInstance.apiDD.getChampionData(championName)

        return response.data[championName]?.let { champData ->
            val validSkins = champData.skins.filter { it.num != 0 }
            val selectedSkin = if (validSkins.isNotEmpty()) {
                validSkins.random()
            } else {
                champData.skins.first()
            }
            "${championName}_${selectedSkin.num}"
        } ?: run {
            Log.e("SearchPlayerVM", "Champion data not found for $championName")
            "${championName}_0"
        }
    }



}
