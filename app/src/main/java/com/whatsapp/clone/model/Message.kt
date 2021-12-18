package com.whatsapp.clone.model

import com.google.firebase.Timestamp

data class Message(
    var sender_id: String = "",
    var received_id: String = "",
    var message: String = "",
    var message_send_time: Timestamp? = null,
    var message_read_state: Boolean? = false,
    var image_url: String? = "",
    var message_id: String = ""
)
