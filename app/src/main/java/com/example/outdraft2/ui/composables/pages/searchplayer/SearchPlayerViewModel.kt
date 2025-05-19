package com.example.outdraft2.ui.composables.pages.searchplayer

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outdraft2.api.RiotApiInstance
import kotlinx.coroutines.launch

class SearchPlayerViewModel : ViewModel() {
    val kda = mutableStateOf("¡Busca el invocador que quieras!")

    val isLoading = mutableStateOf(false)

    fun getProfileIconAndLevel(
        gameName: String,
        tagLine: String,
        onResult: (Int, Int, String, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val apiKey = "RGAPI-b2883465-e745-4c1e-a30c-a15de3a49c76"
                val account = RiotApiInstance.api.getAccountByRiotId(gameName, tagLine, apiKey)
                val info = RiotApiInstance.api.getAccountInfoByPUUID(account.puuid, apiKey)
                onResult(info.profileIconId, info.summonerLevel, account.gameName, account.tagLine)
            } catch (e: Exception) {
                Log.e("SearchPlayerVM", "Error al obtener datos del invocador", e)
            }
        }
    }

}