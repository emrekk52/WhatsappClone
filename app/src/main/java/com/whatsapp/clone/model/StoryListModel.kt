package com.whatsapp.clone.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp


data class StoryListModel(
    val model: MyProfileModel? = null,
    val storyList: Array<StoryModel>? = null
) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(MyProfileModel::class.java.classLoader),
        parcel.createTypedArray(StoryModel)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(model, flags)
        parcel.writeTypedArray(storyList, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StoryListModel> {
        override fun createFromParcel(parcel: Parcel): StoryListModel {
            return StoryListModel(parcel)
        }

        override fun newArray(size: Int): Array<StoryListModel?> {
            return arrayOfNulls(size)
        }
    }
}