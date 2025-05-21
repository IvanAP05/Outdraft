package com.example.outdraft2.api

import com.example.outdraft2.api.data.ChampionDataResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiDataDragon {
    @GET("https://ddragon.leagueoflegends.com/cdn/15.10.1/data/en_US/champion/{championName}.json")
    suspend fun getChampionData(
        @Path("championName") championName: String
    ): ChampionDataResponse
}