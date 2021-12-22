package com.whatsapp.clone.model

import com.google.firebase.Timestamp


data class CallModel(
    val call_type: String = "",
    val profile: MyProfileModel? = null,
    val call_id: Int = 0,
    val elapsedTime: String = "",
    val call_time: Timestamp? = null
)
