package com.whatsapp.clone.databaseprocess

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import com.whatsapp.clone.model.MyProfileModel
import com.google.firebase.storage.ktx.storage
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.viewModel.ProfileUpdateCRUD
import java.util.*

class firebase_CRUD {


    val database: FirebaseFirestore
    val storage: StorageReference
    val auth: FirebaseAuth

    init {
        database = Firebase.firestore
        storage = Firebase.storage.reference
        auth = Firebase.auth
    }

    fun getAuth(): Boolean {

        if (auth.currentUser == null)
            return false
        else
            return true

    }

    fun getCurrentId(): String {
        return auth.currentUser?.uid!!
    }


    fun uploadImage(
        profile: MyProfileModel,
        chooseUri: Uri?,
        viewModel: ProfileUpdateCRUD,
        dialog: AlertDialog,
        nav: NavController,
        context: Context
    ) {

        profile.registration_date = Timestamp.now()
        profile.profile_id = getCurrentId()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            profile.token = token
        })


        if (!chooseUri.toString().equals("null") || !Uri.EMPTY.equals(chooseUri)) {
            val ref = storage.child("user_photo/${UUID.randomUUID()}}")
            if (chooseUri != null) {
                ref.putFile(chooseUri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }

                    ref.downloadUrl
                }.addOnCompleteListener { task ->

                    if (task.isSuccessful)
                        task.result.toString().also {
                            profile.profile_photo = it
                            viewModel.getPhotoUrl(
                                profile, dialog, database, nav
                            )
                        }


                }.addOnFailureListener {
                    context.toastShow(it.message.toString())
                    dialog.dismiss()
                }.addOnCanceledListener {
                    dialog.dismiss()
                }
            }

        } else
            viewModel.getPhotoUrl(profile, dialog, database, nav)
    }


    fun getTimestamp() = Timestamp.now()


}




