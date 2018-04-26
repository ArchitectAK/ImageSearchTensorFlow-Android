package com.cogitator.imagesearchtensorflowandroid.network

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 26/04/2018 (MM/DD/YYYY)
 */
class APIClient {

    fun getClient(): Retrofit {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
                .baseUrl("https://gist.github.com/AnkitDroidGit/8b41329bec8da04c2b7b8d509f0fb84d/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
    }
}