package com.whatsapp.clone.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class StoryModel(
    val story_url: String? = "",
    val story_time: Timestamp? = null,
    val story_description: String? = "",
    val id: String? = "",
    var seenProfile: List<MyProfileModel>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(MyProfileModel)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(story_url)
        parcel.writeParcelable(story_time, flags)
        parcel.writeString(story_description)
        parcel.writeString(id)
        parcel.writeTypedList(seenProfile)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StoryModel> {
        override fun createFromParcel(parcel: Parcel): StoryModel {
            return StoryModel(parcel)
        }

        override fun newArray(size: Int): Array<StoryModel?> {
            return arrayOfNulls(size)
        }
    }
}