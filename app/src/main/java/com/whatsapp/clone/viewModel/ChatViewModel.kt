package com.whatsapp.clone.viewModel

import android.app.AlertDialog
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.gson.Gson
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.extensions.decryptWithAES
import com.whatsapp.clone.extensions.encrypt
import com.whatsapp.clone.extensions.getServerTime
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.model.*
import com.whatsapp.clone.notification.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel : ViewModel() {


    var messageInputText = MutableLiveData<String>()
    var db_error = MutableLiveData<String>()

    val chat_list = MutableLiveData<List<Message>>()
    val seen = MutableLiveData<String>()
    private var _profile = MyProfileModel()

    val crud: firebase_CRUD


    init {
        crud = firebase_CRUD()
        messageInputText.value = ""
        getMyProfile()
    }

    private fun getMyProfile() {
        if (crud.auth.currentUser != null)
            crud.database.collection("users").document(crud.getCurrentId())
                .addSnapshotListener { value, error ->

                    val profile = MyProfileModel(
                        value?.get("name").toString(),
                        value?.get("profile_photo").toString(),
                        value?.get("profile_id").toString(),
                        value?.get("phone_number").toString(),
                        value?.get("status").toString(),
                        value?.get("token").toString(),
                    )

                    _profile = profile

                }
    }

    fun sendMessage(model: MyProfileModel) {

        if (!messageInputText.value.toString().trim().equals("")) {
            val crud = firebase_CRUD()
            val message = Message(
                crud.getCurrentId(),
                model.profile_id!!,
                messageInputText.value.toString().encrypt()!!,
                Timestamp.now(),
                message_read_state = if (model.roaming.equals("çevrimiçi")) true else false
            )



            crud.database.collection("channel").add(message).addOnCompleteListener {
                if (it.isSuccessful) {
                    crud.database.collection("channel").document(it.result?.id!!)
                        .update("message_id", it.result?.id!!)
                    sendNotification(
                        PushNotification(
                            NotificationData(
                                _profile.name!!,
                                message.message.decryptWithAES().toString()
                            ),
                            model.token.toString()
                        )
                    )
                }
            }

        }
    }


    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {

            println("token " + notification.to)
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful)
                    Log.d(TAG, "Response : ${Gson().toJson(response)}")
                else
                    Log.e(TAG, response.errorBody().toString())

                println(response.errorBody().toString())

            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }


        }

    fun sendMessage(
        message: String,
        id: String,
        chooseUri: Uri,
        context: Context,
        nav: NavController,
        dialog: AlertDialog
    ) {
        val crud = firebase_CRUD()
        val ref = crud.storage.child("message_photo")

        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, chooseUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

        ref.putBytes(reducedImage).continueWithTask { task ->

            if (!task.isSuccessful)
                task.exception?.let {
                    throw it
                }


            ref.downloadUrl
        }.addOnCompleteListener { task ->

            if (task.isSuccessful)
                task.result.toString().also {
                    val message = Message(
                        crud.getCurrentId(),
                        id,
                        message.encrypt()!!,
                        Timestamp.now(),
                        image_url = it
                    )
                    crud.database.collection("channel").add(message).addOnCompleteListener {
                        if (it.isSuccessful) {
                            crud.database.collection("channel").document(it.result?.id!!)
                                .update("message_id", it.result?.id!!)
                        } else
                            context.toastShow(it.exception.toString())
                    }
                    dialog.dismiss()
                    nav.navigateUp()
                }

        }.addOnFailureListener {
            context.toastShow(it.message.toString())
            dialog.dismiss()
        }.addOnCanceledListener {
            dialog.dismiss()
        }

    }


    fun getMessage(received_id: String) {
        val list = arrayListOf<Message>()

        var tempDay = 0


        crud.database.collection("channel").orderBy(
            "message_send_time",
            Query.Direction.ASCENDING
        ).addSnapshotListener { snapshot, exception ->


            if (exception != null)
                db_error.value = exception.localizedMessage.toString()
            else {
                list.clear()
                snapshot?.documents?.forEach {


                    val message = it.toObject(Message::class.java)!!

                    if (message.received_id.equals(crud.getCurrentId()) && message.sender_id
                            .equals(received_id) ||
                        message.received_id
                            .equals(received_id) && message.sender_id
                            .equals(crud.getCurrentId())
                    ) {

                        val calendar: Calendar = Calendar.getInstance()
                        calendar.timeInMillis =
                            message.message_send_time?.toDate()?.time!!

                        val d = calendar.get(Calendar.DAY_OF_MONTH)

                        if (tempDay != d) {
                            tempDay = d
                            list.add(
                                Message(
                                    "",
                                    "",
                                    "",
                                    message.message_send_time
                                )
                            )
                        }

                        list.add(
                            message
                        )

                    }


                }
                chat_list.value = list
            }

        }

    }


    fun checkLastSeen(user_id: String) {

        crud.database.collection("users").document(user_id)
            .addSnapshotListener { snapshot, except ->
                seen.value = snapshot?.get("roaming").toString()
            }


    }

    fun updateLastSeen(pr: Int) {
        when (pr) {

            1 -> {
                crud.database.collection("users").document(crud.getCurrentId())
                    .update("roaming", "çevrimiçi")
            }


            2 -> {
                crud.database.collection("users").document(crud.getCurrentId())
                    .update("roaming", getServerTime().toDate().time)
            }

            3 -> {
                crud.database.collection("users").document(crud.getCurrentId())
                    .update("roaming", "yazıyor..")
            }

        }

    }


}