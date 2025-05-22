package com.example.outdraft2.api

import com.example.outdraft2.api.data.SummonerInfo
import com.example.outdraft2.api.data.ChampionMastery
import com.example.outdraft2.api.data.PlayerRank
import com.example.outdraft2.api.data.MatchResponse
import com.example.outdraft2.api.data.RiotAccount
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

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
    ): SummonerInfo

    @GET("https://euw1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/{encryptedPUUID}/top?count=1")
    suspend fun getTopChampByPUUID(
        @Path("encryptedPUUID") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): List<ChampionMastery>

    @GET("https://europe.api.riotgames.com/lol/match/v5/matches/by-puuid/{puuid}/ids")
    suspend fun getMatchesListByPUUID(
        @Path("puuid") puuid: String,
        @Header("X-Riot-Token") apiKey: String,
        @Query("start") start: Int = 0,
        @Query("count") count: Int = 20
    ): List<String>

    @GET("https://europe.api.riotgames.com/lol/match/v5/matches/{matchId}")
    suspend fun getMatchInfoById(
        @Path("matchId") matchId: String,
        @Header("X-Riot-Token") apiKey: String
    ): MatchResponse

    @GET("https://euw1.api.riotgames.com/lol/league/v4/entries/by-puuid/{encryptedPUUID}")
    suspend fun getRankByPUUID(
        @Path("encryptedPUUID") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): List<PlayerRank>
}