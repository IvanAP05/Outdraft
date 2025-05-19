package com.example.outdraft2.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RiotApiInstance {

    val api: RiotApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://placeholder.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RiotApi::class.java)
    }
}