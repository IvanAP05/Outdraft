package com.example.outdraft.domain

import android.content.Context
import com.example.outdraft.data.LocalDataSource

object MainRepository {

    fun getFavoriteChampions(context: Context): Set<String> {
        return LocalDataSource.getFavoriteChampions(context)
    }

    fun addFavoriteChampion(context: Context, championId: String): Set<String> {
        val currentFavorites = LocalDataSource.getFavoriteChampions(context).toMutableSet()
        currentFavorites.add(championId)
        LocalDataSource.saveFavoriteChampions(context, currentFavorites)
        return currentFavorites
    }

    fun removeFavoriteChampion(context: Context, championId: String): Set<String> {
        val currentFavorites = LocalDataSource.getFavoriteChampions(context).toMutableSet()
        currentFavorites.remove(championId)
        LocalDataSource.saveFavoriteChampions(context, currentFavorites)
        return currentFavorites
    }
}