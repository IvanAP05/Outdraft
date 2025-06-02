package com.example.outdraft.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RiotApiInstance {

    val apiRiot: ApiRIOT by lazy {
        Retrofit.Builder()
            .baseUrl("https://placeholder.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiRIOT::class.java)
    }

    val apiDD: ApiDataDragon by lazy {
        Retrofit.Builder()
            .baseUrl("https://placeholder.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiDataDragon::class.java)
    }
}