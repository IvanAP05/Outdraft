package com.example.outdraft2.api

import com.example.outdraft2.api.data.AccountInfo
import com.example.outdraft2.api.data.ChampionMastery
import com.example.outdraft2.api.data.RiotAccount
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiRIOT {

    @GET("https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
    suspend fun getAccountByRiotId(
        @Path("gameName") gameName: String,
        @Path("tagLine") tagLine: String,
        @Header("X-Riot-Token") apiKey: String
    ): RiotAccount

    @GET("https://euw1.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/{puuid}")
    suspend fun getAccountInfoByPUUID(
        @Path("puuid") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): AccountInfo

    @GET("https://euw1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/{encryptedPUUID}/top?count=1")
    suspend fun getTopChampByPUUID(
        @Path("encryptedPUUID") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): List<ChampionMastery>

}

