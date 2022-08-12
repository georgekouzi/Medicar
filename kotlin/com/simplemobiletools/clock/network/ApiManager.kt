package com.simplemobiletools.clock.network

import android.util.Log
import com.simplemobiletools.clock.helpers.BASE_URL
import com.simplemobiletools.clock.network.model.PillBoxModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiManager {

    @GET("button/0")
    suspend fun boxOne(): PillBoxModel

    @GET("button/1")
    suspend fun boxTwo(): PillBoxModel

    @GET("led/0/true")
    suspend fun boxOneOpenLed()

    @GET("led/1/true")
    suspend fun boxTwoOpenLed()

    @GET("led/0/false")
    suspend fun boxOneCloseLed()

    @GET("led/1/false")
    suspend fun boxTwoCloseLed()

//    http://192.168.1.31:8000/led/0  led 0 status
//    http://192.168.1.31:8000/led/1  led 1 status
//
//    http://192.168.1.31:8000/led/0/true led 0 on
//    http://192.168.1.31:8000/led/1/true led 1 on
    // http://192.168.1.31:8000/led/0/false led 0 off
//    http://192.168.1.31:8000/led/1/false led 1 off
//
//    http://192.168.1.31:8000/button/0 button 0 status
//    http://192.168.1.31:8000/button/1 button 1 status



    companion object {

        fun create(): ApiManager {
            val client = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor())
                .build()
            return Retrofit
                .Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiManager::class.java)
        }
    }
}


class TokenInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var original = chain.request()
        val url = original.url().newBuilder().build()
        original = original.newBuilder().url(url).build()
        return chain.proceed(original)
    }
}


//http://data.fixer.io/api/latest?access_key=a09197b882e3c2d55792d966d90e004f&format=1
