package com.whatsapp.clone.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.extensions.decryptWithAES
import com.whatsapp.clone.model.Message
import com.whatsapp.clone.model.MyProfileModel
import kotlinx.coroutines.handleCoroutineException
import java.sql.Time

class ProfileViewModel : ViewModel() {


    val profile = MutableLiveData<MyProfileModel>()


    val crud by lazy {
        firebase_CRUD()
    }


    fun getProfile(id: String) {

        crud.database.collection("users").document(id).addSnapshotListener { value, error ->

            val prof = MyProfileModel(
                value?.get("name").toString(),
                value?.get("profile_photo").toString(),
                value?.get("profile_id").toString(),
                value?.get("phone_number").toString(),
                value?.get("status").toString(),
                value?.get("roaming").toString()
            )
            prof.registration_date = value?.get("registration_date") as Timestamp

            profile.postValue(prof)

        }


    }


}


