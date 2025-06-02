package com.example.outdraft.ui.utils

import android.content.Context
import com.example.outdraft.api.data.Champion
import com.example.outdraft.api.data.Item
import com.example.outdraft.common.RIOT_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

fun readJsonFromAssets(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}

fun getChampionNameFromId(context: Context, championId: String, fileName: String = "data/champion_ES.json"): String? {
    val json = readJsonFromAssets(context, fileName)
    val root = JSONObject(json)
    val data = root.getJSONObject("data")

    val keys = data.keys()
    for (key in keys) {
        val champ = data.getJSONObject(key)
        if (champ.getString("key") == championId) {
            return champ.getString("id")
        }
    }

    return null
}

suspend fun loadChampionsFromJson(context: Context, fileName: String = "data/champion_ES.json"): List<Champion> {
    return withContext(Dispatchers.IO) {
        val jsonString = readJsonFromAssets(context, fileName)
        val jsonObject = JSONObject(jsonString)
        val dataObject = jsonObject.getJSONObject("data")
        val champions = mutableListOf<Champion>()

        val keys = dataObject.keys()
        while (keys.hasNext()) {
            val championKey = keys.next()
            try {
                val championJson = dataObject.getJSONObject(championKey)
                val tagsArray = championJson.getJSONArray("tags")
                val tags = List(tagsArray.length()) { tagsArray.getString(it) }

                val champion = Champion(
                    key = championJson.getString("key"),
                    id = championJson.getString("id"),
                    name = championJson.getString("name"),
                    tags = tags
                )

                champions.add(champion)
            } catch (e: Exception) {
                continue
            }
        }

        champions.sortedBy { it.name }
    }
}


suspend fun loadItemsFromJson(context: Context, fileName: String = "data/item_ES.json"): List<Item> {
    return withContext(Dispatchers.IO) {
        val jsonString = readJsonFromAssets(context, fileName)
        val rootObject = JSONObject(jsonString)
        val dataObject = rootObject.getJSONObject("data")
        val items = mutableListOf<Item>()

        val keys = dataObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val itemJson = dataObject.getJSONObject(key)

            if (!itemJson.has("name") || itemJson.getString("name").isBlank()) continue

            val item = Item(
                id = key,
                name = itemJson.getString("name"),
                imageUrl = "https://ddragon.leagueoflegends.com/cdn/$RIOT_VERSION/img/item/$key.png",
                tags = itemJson.getJSONArray("tags").let { tagsArray ->
                    List(tagsArray.length()) { tagsArray.getString(it) }
                }.toString()
            )

            items.add(item)
        }

        items
    }
}
