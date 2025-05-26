package com.example.outdraft2.api.data

data class RiotAccount(
    val puuid: String,
    val gameName: String,
    val tagLine: String
)

data class SummonerInfo(
    val profileIconId: Int,
    val summonerLevel: Int
)

data class Champion(
    val id: String,
    val name: String,
    val tags: List<String>
) {
    val imageUrl: String
        get() = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${id}_0.jpg"
}

data class ChampionMastery(
    val championId: Int,
    val championLevel: Int,
    val championPoints: Int
)

data class ChampionDataResponse(
    val data: Map<String, ChampionData>
)

data class ChampionData(
    val id: String,
    val skins: List<Skin>,
    val name: String
)

data class Skin(
    val id: String,
    val num: Int,
    val name: String
)

data class PlayerRank(
    val leagueId: String,
    val queueType: String,
    val tier: String,
    val rank: String,
    val summonerId: String,
    val summonerName: String,
    val leaguePoints: Int,
    val wins: Int,
    val losses: Int,
    val veteran: Boolean,
    val inactive: Boolean,
    val freshBlood: Boolean,
    val hotStreak: Boolean
)

data class MatchParticipant(
    val win: Boolean,
    val championName: String,
    val championId: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val summoner1Id: Int = 0,
    val summoner2Id: Int = 0,
    val summonerName: String = "",
    val riotIdGameName: String = "",
    val riotIdTagline: String = "",
    val profileIcon: Int = 0,
    val championLevel: Int = 1,
    val totalMinionsKilled: Int = 0,
    val neutralMinionsKilled: Int = 0,
    val goldEarned: Int = 0,
    val totalDamageDealtToChampions: Int = 0,
    val teamId: Int = 100,
    val item0: Int = 0,
    val item1: Int = 0,
    val item2: Int = 0,
    val item3: Int = 0,
    val item4: Int = 0,
    val item5: Int = 0
)

data class MatchTeam(
    val teamId: Int,
    val win: Boolean,
    val bans: List<ChampionBan> = emptyList()
)

data class ChampionBan(
    val championId: Int,
    val pickTurn: Int
)

data class MatchSummary(
    val matchId: String,
    val gameDuration: String,
    val gameType: String,
    val queueId: Int,
    val gameMode: String,
    val playerStats: MatchParticipant,
    val isWin: Boolean,
    val gameCreation: Long = 0L
)

data class MatchDetail(
    val matchId: String,
    val gameDuration: String,
    val gameType: String,
    val gameMode: String,
    val queueId: Int,
    val gameCreation: Long,
    val participants: List<MatchParticipant>,
    val teams: List<MatchTeam> = emptyList()
) {
    fun getTeam1(): List<MatchParticipant> = participants.filter { it.teamId == 100 }
    fun getTeam2(): List<MatchParticipant> = participants.filter { it.teamId == 200 }
}

data class MatchResponse(
    val metadata: MatchMetadata,
    val info: MatchInfo
)

data class MatchMetadata(
    val matchId: String,
    val participants: List<String>
)

data class MatchInfo(
    val gameId: Long,
    val gameDuration: Long,
    val gameType: String,
    val gameMode: String,
    val queueId: Int,
    val gameCreation: Long,
    val participants: List<MatchParticipant>,
    val teams: List<MatchTeam> = emptyList()
)

data class PlayerState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val matchSummaries: List<MatchSummary> = emptyList(),
    val error: String? = null,
    val hasMoreMatches: Boolean = true,
    val soloQueueRank: PlayerRank? = null,
    val flexQueueRank: PlayerRank? = null,
    val selectedMatchDetail: MatchDetail? = null,
    val isLoadingMatchDetail: Boolean = false
)

