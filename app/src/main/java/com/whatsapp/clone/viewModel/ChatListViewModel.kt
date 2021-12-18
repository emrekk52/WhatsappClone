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

class ChatListViewModel : ViewModel() {

    val crud: firebase_CRUD

    val users = arrayListOf<MyProfileModel>()


    init {
        crud = firebase_CRUD()
    }

    var modelChat = MutableLiveData<List<MyProfileModel>>()
    var db_error = MutableLiveData<String>()


    fun getChatList() {

        val list = arrayListOf<String>()

        crud.database.collection("channel").addSnapshotListener { snap, excp ->

            if (excp == null) {
                list.clear()
                snap?.documents?.forEach {

                    if (it["received_id"].toString().equals(crud.getCurrentId()))
                        list.add(it["sender_id"].toString())

                    if (it["sender_id"].toString().equals(crud.getCurrentId()))
                        list.add(it["received_id"].toString())

                }
                readChats(list)
            } else
                db_error.value = excp.localizedMessage.toString()

        }


    }


    private fun readChats(list: ArrayList<String>) {


        crud.database.collection("users").addSnapshotListener { value, error ->

            if (error == null) {
                users.clear()
                value?.documents?.forEach { doc ->
                    val profile: MyProfileModel = MyProfileModel(
                        doc["name"].toString(),
                        doc["profile_photo"].toString(),
                        doc["profile_id"].toString(),
                        doc["phone_number"].toString(),
                        doc["status"].toString(),
                        doc["roaming"].toString(),
                        doc["token"].toString(),
                    )
                    list.forEach {
                        if (profile.profile_id.equals(it)) {

                            if (users.size != 0)
                                users.forEach {
                                    if (!profile.profile_id.equals(it.profile_id))
                                        users.add(profile)
                                }
                            else
                                users.add(profile)
                        }
                    }


                }


                modelChat.value = users


            }

        }

    }


}


