package com.example.outdraft.data

import android.content.Context
import com.example.outdraft.common.PREFERENCES_KEY_FAVORITES
import com.example.outdraft.common.PREFERENCES_NAME_FILE

object LocalDataSource {

    fun getFavoriteChampions(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFERENCES_NAME_FILE, Context.MODE_PRIVATE)
        return prefs.getStringSet(PREFERENCES_KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun saveFavoriteChampions(context: Context, favorites: Set<String>) {
        val prefs = context.getSharedPreferences(PREFERENCES_NAME_FILE, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putStringSet(PREFERENCES_KEY_FAVORITES, favorites)
            apply()
        }
    }
}