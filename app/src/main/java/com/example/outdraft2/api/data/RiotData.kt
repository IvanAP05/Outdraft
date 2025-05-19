package com.example.outdraft2.api.data

data class MatchResponse(
    val info: Info
)

data class Info(
    val participants: List<Participant>
)

data class Participant(
    val puuid: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val riotIdGameName: String
)

data class RiotAccount(
    val puuid: String,
    val gameName: String,
    val tagLine: String
)

data class AccountInfo(
    val profileIconId: Int,
    val summonerLevel: Int
)
