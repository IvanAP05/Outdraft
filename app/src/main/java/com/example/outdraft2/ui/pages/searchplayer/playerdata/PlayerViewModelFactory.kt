package com.example.outdraft2.ui.pages.searchplayer.playerdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PlayerViewModelFactory(
    private val summonerName: String,
    private val tagLine: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(summonerName, tagLine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}