package com.example.outdraft.ui.pages.counter.counterdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outdraft.api.SupabaseApiService
import com.example.outdraft.api.data.Champion
import com.example.outdraft.api.data.CounterUiState
import com.example.outdraft.api.data.WinRatio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CounterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    private val supabaseUrl = "https://egrizauyajhrzxxzibpd.supabase.co/"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncml6YXV5YWpocnp4eHppYnBkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MDg4MzcsImV4cCI6MjA2NDI4NDgzN30.8NU291tbHg2q87HCZ3eDuPQn9GRNLzhVoEyVYFdiS28"

    private val supabaseApi = Retrofit.Builder()
        .baseUrl(supabaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SupabaseApiService::class.java)

    private val availableRanks = listOf("IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "EMERALD", "DIAMOND")
    private val availablePositions = listOf("TOP", "JUNGLE", "MID", "BOT", "SUPPORT")

    init {
        _uiState.value = _uiState.value.copy(
            availableRanks = availableRanks,
            availablePositions = availablePositions
        )
    }

    fun updateSelectedRank(rank: String) {
        val newRank = if (rank == "ALL") null else rank
        _uiState.value = _uiState.value.copy(
            selectedRank = newRank
        )
    }

    fun updateSelectedPosition(position: String) {
        val newPosition = if (position == "ALL") null else position
        _uiState.value = _uiState.value.copy(
            selectedPosition = newPosition
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun loadWinRatiosForChampion(championId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val allWinRatios = loadAllWinRatiosFromSupabase(championId)
                _uiState.value = _uiState.value.copy(
                    allWinRatios = allWinRatios,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                val errorMsg = handleSupabaseError(e)
                _uiState.value = _uiState.value.copy(
                    allWinRatios = emptyList(),
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    private suspend fun loadAllWinRatiosFromSupabase(championId: String): List<WinRatio> {
        val championIdNumber = championId.toIntOrNull()
            ?: throw IllegalArgumentException("ID de campeón no válido: $championId. Debe ser un número.")

        try {
            val matchups = supabaseApi.getAllMatchupsForChampion(
                apiKey = supabaseKey,
                authorization = "Bearer $supabaseKey",
                championId = "eq.$championIdNumber"
            )

            if (matchups.isEmpty()) {
                return emptyList()
            }

            val winRatios = matchups.mapNotNull { matchup ->
                try {
                    val enemyChampion = Champion(
                        key = matchup.opponent_id.toString(),
                        name = matchup.opponent_name,
                        id = matchup.opponent_id.toString(),
                        tags = emptyList()
                    )

                    WinRatio(
                        enemyChampion = enemyChampion,
                        winRate = matchup.winrate,
                        gamesPlayed = matchup.games_played,
                        rank = matchup.rank,
                        position = matchup.position
                    )
                } catch (e: Exception) {
                    null
                }
            }

            return winRatios.sortedByDescending { it.winRate }

        } catch (e: Exception) {
            throw e
        }
    }

    private fun handleSupabaseError(exception: Exception): String {
        return when {
            exception.message?.contains("401") == true ||
                    exception.message?.contains("Unauthorized") == true ->
                "Sin permisos para acceder a los datos. Verifica tu configuración de Supabase."

            exception.message?.contains("403") == true ||
                    exception.message?.contains("Forbidden") == true ->
                "Acceso denegado. Verifica las políticas RLS de Supabase."

            exception.message?.contains("500") == true ||
                    exception.message?.contains("Internal Server Error") == true ->
                "Error del servidor. Inténtalo más tarde."

            exception.message?.contains("network") == true ||
                    exception.message?.contains("timeout") == true ->
                "Error de conexión. Verifica tu conexión a internet."

            exception is IllegalArgumentException ->
                exception.message ?: "Error en los datos proporcionados"

            else -> "Error cargando datos: ${exception.message}"
        }
    }
}