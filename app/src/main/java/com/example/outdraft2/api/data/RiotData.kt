package com.example.outdraft2.api.data

data class RiotAccount(
    val puuid: String,
    val gameName: String,
    val tagLine: String
)

data class AccountInfo(
    val profileIconId: Int,
    val summonerLevel: Int,
    val topChampionId: Int
)
data class ChampionMastery(
    val championId: Int,
    val championLevel: Int,
    val championPoints: Int,
)

data class ChampionDataResponse(
    val data: Map<String, ChampionData>
)

data class ChampionData(
    val id: String,
    val skins: List<Skin>
)

data class Skin(
    val id: String,
    val num: Int,
    val name: String,
    val chromas: Boolean
)
