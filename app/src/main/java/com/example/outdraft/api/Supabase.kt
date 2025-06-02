package com.example.outdraft.api

import com.example.outdraft.api.data.MatchupResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SupabaseApiService {
    @GET("rest/v1/matchups")
    suspend fun getAllMatchupsForChampion(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("champion_id") championId: String
    ): List<MatchupResponse>
}