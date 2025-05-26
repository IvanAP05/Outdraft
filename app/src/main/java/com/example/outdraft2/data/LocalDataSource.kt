package com.example.outdraft2.data

import android.content.Context
import com.example.outdraft2.common.PREFERENCES_KEY_FAVORITES
import com.example.outdraft2.common.PREFERENCES_NAME_FILE

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