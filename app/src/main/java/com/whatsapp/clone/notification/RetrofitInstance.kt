package com.whatsapp.clone.notification

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.whatsapp.clone.notification.Constants.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface RetrofitInstance {

    companion object {


        private val retrofit by lazy {
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()
        }


        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }


    }
}


