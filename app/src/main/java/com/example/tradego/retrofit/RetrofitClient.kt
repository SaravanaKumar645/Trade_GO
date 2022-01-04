package com.example.tradego.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private  var instance:Retrofit?=null
    private val okhttp_client=OkHttpClient
        .Builder()
        .readTimeout(1,TimeUnit.MINUTES)
        .writeTimeout(1,TimeUnit.MINUTES)
        .retryOnConnectionFailure(true)
        .callTimeout(1,TimeUnit.MINUTES)
        .build()
    fun getInstance(): RetrofitInterface {
        if(instance==null){
            instance=Retrofit.Builder()
                .baseUrl("https://tradego-android-server.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okhttp_client)
                .build()

        }
        return instance!!.create(RetrofitInterface::class.java)
    }

}
//https://trade-go.herokuapp.com