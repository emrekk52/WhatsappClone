package com.whatsapp.clone.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "profile_table")
data class ProfileDatabase(

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "profile_photo")
    var profile_photo: String,

    @ColumnInfo(name = "profile_id")
    var profile_id: String,

    @ColumnInfo(name = "phone_number")
    var phone_number: String,
    @ColumnInfo(name = "token")
    var token: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
