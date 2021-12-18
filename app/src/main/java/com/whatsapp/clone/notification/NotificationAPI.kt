package com.whatsapp.clone.notification

import com.squareup.okhttp.ResponseBody
import com.whatsapp.clone.notification.Constants.Companion.CONTENT_TYPE
import com.whatsapp.clone.notification.Constants.Companion.SERVER_KEY
import com.whatsapp.clone.model.PushNotification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {


    @Headers("Authorization:key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>

}