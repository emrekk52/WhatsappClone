package com.whatsapp.clone.viewModel

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.ui.login.ProfileFragmentDirections

class ProfileUpdateCRUD : ViewModel() {


    fun getPhotoUrl(
        profile: MyProfileModel,
        dialog: AlertDialog,
        database: FirebaseFirestore,
        nav: NavController
    ) {

        database.collection("users").document(profile.profile_id!!).set(profile)
            .addOnCompleteListener {

                if (it.isSuccessful)
                    dialog.dismiss().also {
                        nav.navigate(
                            ProfileFragmentDirections.actionNavProfileEditToNavHome()
                        )
                    }



            }.addOnFailureListener{
                dialog.dismiss()
            }.addOnCanceledListener {
                dialog.dismiss()
            }

    }


}