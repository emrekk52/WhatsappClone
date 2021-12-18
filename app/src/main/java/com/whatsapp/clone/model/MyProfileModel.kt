package com.whatsapp.clone.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.io.Serializable

data class MyProfileModel(
    val name: String? = "",
    var profile_photo: String? = "https://firebasestorage.googleapis.com/v0/b/whatsapp-clone-6fa32.appspot.com/o/no_profile_photo.png?alt=media&token=01de768a-2c90-4c3a-ac14-7e53fb60c2e6",
    var profile_id: String? = "",
    var phone_number: String? = "",
    val status: String? = "Hey there! I am using WhatsApp.",
    var roaming: String? = "çevrimiçi",
    var token: String? = ""
) : Parcelable {


    var registration_date: Timestamp? = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
        registration_date = parcel.readParcelable(Timestamp::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(profile_photo)
        parcel.writeString(profile_id)
        parcel.writeString(phone_number)
        parcel.writeString(status)
        parcel.writeString(roaming)
        parcel.writeParcelable(registration_date, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyProfileModel> {
        override fun createFromParcel(parcel: Parcel): MyProfileModel {
            return MyProfileModel(parcel)
        }

        override fun newArray(size: Int): Array<MyProfileModel?> {
            return arrayOfNulls(size)
        }
    }


}
