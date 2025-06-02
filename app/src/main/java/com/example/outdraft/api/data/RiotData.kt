package com.example.outdraft.api.data

import android.annotation.SuppressLint
import com.example.outdraft.common.RIOT_VERSION

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
    val key: String,
    val id: String,
    val name: String,
    val tags: List<String>
) {
    val imageSplashUrl: String
        get() = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${id}_0.jpg"
    val imageIconUrl: String
        get() = "https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/img/champion/${id}.png"

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

data class Item(
    val id: String,
    val name: String,
    val imageUrl: String,
    val tags: String
)

data class MatchupResponse(
    val id: String,
    val rank: String,
    val position: String,
    val champion_id: Int,
    val champion_name: String,
    val opponent_id: Int,
    val opponent_name: String,
    val games_played: Int,
    val games_won: Int,
    val winrate: Double
)
data class WinRatio(
    val enemyChampion: Champion,
    val winRate: Double,
    val gamesPlayed: Int,
    val rank: String = "",
    val position: String = ""
)

data class CounterUiState(
    val allWinRatios: List<WinRatio> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableRanks: List<String> = emptyList(),
    val availablePositions: List<String> = emptyList(),
    val selectedRank: String? = null,
    val selectedPosition: String? = null,
    val searchQuery: String = ""
) {
    val filteredWinRatios: List<WinRatio>
        get() {

            val minGamesFiltered = allWinRatios.filter { winRatio ->
                winRatio.gamesPlayed >= 5
            }


            val basicFiltered = minGamesFiltered.filter { winRatio ->
                val rankMatches = selectedRank == null ||
                        winRatio.rank.equals(selectedRank, ignoreCase = true)

                val expectedDbPosition = mapPositionToDbValue(selectedPosition)
                val positionMatches = selectedPosition == null ||
                        expectedDbPosition.equals(winRatio.position, ignoreCase = true)

                val searchMatches = searchQuery.isEmpty() ||
                        winRatio.enemyChampion.name.contains(searchQuery, ignoreCase = true)

                rankMatches && positionMatches && searchMatches
            }

            val finalFiltered = if (selectedRank == null) {
                groupAndSumByChampion(basicFiltered)
            } else {
                basicFiltered
            }

            val sorted = finalFiltered.sortedByDescending { it.winRate }

            return sorted
        }

    @SuppressLint("DefaultLocale")
    private fun groupAndSumByChampion(winRatios: List<WinRatio>): List<WinRatio> {
        return winRatios.groupBy { winRatio ->
            if (selectedPosition != null) {
                "${winRatio.enemyChampion.id}_${winRatio.position}"
            } else {
                winRatio.enemyChampion.id
            }
        }.map { (_, groupedWinRatios) ->
            val firstWinRatio = groupedWinRatios.first()
            val totalGamesPlayed = groupedWinRatios.sumOf { it.gamesPlayed }
            val totalGamesWon = groupedWinRatios.sumOf {
                it.gamesPlayed * it.winRate / 100.0
            }
            val aggregatedWinRate = if (totalGamesPlayed > 0) {
                (totalGamesWon / totalGamesPlayed.toDouble()) * 100.0
            } else {
                0.0
            }

            WinRatio(
                enemyChampion = firstWinRatio.enemyChampion,
                winRate = aggregatedWinRate,
                gamesPlayed = totalGamesPlayed,
                rank = "ALL",
                position = if (selectedPosition != null) firstWinRatio.position else "ALL"
            )
        }.filter { it.gamesPlayed >= 5 }
    }

    private fun mapPositionToDbValue(position: String?): String {
        return when(position?.uppercase()) {
            "TOP" -> "top"
            "JUNGLE" -> "jungle"
            "MID" -> "mid"
            "BOT" -> "bot"
            "SUPPORT" -> "support"
            else -> position ?: ""
        }
    }
}