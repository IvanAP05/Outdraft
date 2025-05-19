package com.example.outdraft2.api

import com.example.outdraft2.api.data.AccountInfo
import com.example.outdraft2.api.data.MatchResponse
import com.example.outdraft2.api.data.RiotAccount
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


interface RiotApi {
    @GET("https://europe.api.riotgames.com/lol/match/v5/matches/{matchId}")
    suspend fun getMatchDetails(
        @Path("matchId") matchId: String,
        @Header("X-Riot-Token") apiKey: String
    ): MatchResponse

    @GET("https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
    suspend fun getAccountByRiotId(
        @Path("gameName") gameName: String,
        @Path("tagLine") tagLine: String,
        @Header("X-Riot-Token") apiKey: String
    ): RiotAccount

    @GET("https://europe.api.riotgames.com/lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count=20")
    suspend fun getMatchIdsByPUUID(
        @Path("puuid") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): List<String>

    @GET("https://euw1.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/{puuid}")
    suspend fun getAccountInfoByPUUID(
        @Path("puuid") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): AccountInfo
}

