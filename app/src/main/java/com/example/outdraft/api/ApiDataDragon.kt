package com.example.outdraft.api

import com.example.outdraft.api.data.ChampionDataResponse
import com.example.outdraft.common.RIOT_VERSION
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiDataDragon {
    @GET("https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/data/en_US/champion/{championName}.json")
    suspend fun getChampionData(
        @Path("championName") championName: String
    ): ChampionDataResponse
}