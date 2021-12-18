package com.whatsapp.clone.viewModel

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.whatsapp.clone.dao.ContactDao
import com.whatsapp.clone.dao.ContactRepository
import com.whatsapp.clone.dao.ContactRoomDatabase
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.model.ProfileDatabase
import com.whatsapp.clone.model.StoryListModel
import com.whatsapp.clone.model.StoryModel
import java.io.ByteArrayOutputStream
import java.util.*

class StoryViewModel(application: Application) : AndroidViewModel(application) {

    val crud: firebase_CRUD
    val my_profile = MutableLiveData<MyProfileModel>()
    val myStory = MutableLiveData<List<StoryModel>>()

    val profile: LiveData<List<ProfileDatabase>>

    val stories = MutableLiveData<List<StoryListModel>>()

    val story_list = arrayListOf<StoryListModel>()

    val dao: ContactDao
    val repo: ContactRepository

    init {
        crud = firebase_CRUD()
        getMyProfile()
        getMyStories()
        dao = ContactRoomDatabase.invoke(application).contactDao()
        repo = ContactRepository(dao)
        profile = repo.contact
    }

    fun getMyProfile() {
        crud.database.collection("users").document(crud.getCurrentId())
            .addSnapshotListener { value, error ->

                if (error == null) {
                    my_profile.value = MyProfileModel(
                        value?.get("name")!!.toString(),
                        value["profile_photo"].toString(),
                        value["profile_id"].toString(),
                        value["phone_number"].toString(),
                        value["status"].toString()
                    )
                }
            }

    }


    fun uploadStory(photo_uri: Uri, context: Context, alertDialog: AlertDialog) {

        val ref = crud.storage.child("stories/${UUID.randomUUID()}}")
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, photo_uri)
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
                    crud.database.collection("story${crud.getCurrentId()}").add(
                        StoryModel(it, crud.getTimestamp())
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            crud.database.collection("story${crud.getCurrentId()}")
                                .document(it.result?.id!!).update("id", it.result?.id!!)
                        }
                    }
                    alertDialog.dismiss()
                    context.toastShow("Hikaye yüklendi!")
                }

        }.addOnFailureListener {
            context.toastShow(it.localizedMessage.toString())
            alertDialog.dismiss()
        }.addOnCanceledListener {
            alertDialog.dismiss()
        }


    }


    fun getMyStories() {

        val list = arrayListOf<StoryModel>()
        crud.database.collection("story${crud.getCurrentId()}")
            .orderBy("story_time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                list.clear()
                if (error == null) {

                    value?.documents?.forEach {
                        val storyModel = it.toObject(StoryModel::class.java)
                        list.add(storyModel!!)
                    }

                    list.forEach { storyModel ->

                        crud.database.collection("seen${storyModel.id}")
                            .addSnapshotListener { value, error ->
                                val profile_list = arrayListOf<MyProfileModel>()

                                if (error == null) {
                                    value?.documents?.forEach {

                                        crud.database.collection("users").document(it.id)
                                            .addSnapshotListener { c, er ->
                                                if (er == null) {

                                                    val t = MyProfileModel(
                                                        c!!["name"].toString(),
                                                        c["profile_photo"].toString(),
                                                        c["profile_id"].toString(),
                                                        status = c["status"].toString()

                                                    )
                                                    profile_list.add(t)
                                                    storyModel.seenProfile = profile_list
                                                }

                                            }

                                    }

                                }

                            }


                    }


                    myStory.value = list

                }

            }

    }


    fun getOtherStories(listModel: List<ProfileDatabase>) {
        story_list.clear()
        listModel.forEach {




            crud.database.collection("story${it.profile_id}")
                .orderBy("story_time", Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error == null) {
                        val list = arrayListOf<StoryModel>()
                        var profile = MyProfileModel(it.name, it.profile_photo, it.profile_id, it.phone_number)

                        value?.documents?.forEach {

                            val stories = StoryModel(
                                it["story_url"].toString(),
                                it["story_time"] as Timestamp,
                                it["story_description"].toString(),
                                it["id"].toString(),
                            )
                            list.add(stories)
                        }
                        story_list.add(StoryListModel(profile, list.toTypedArray()))
                        stories.value = story_list


                    }


                }


        }


    }


}