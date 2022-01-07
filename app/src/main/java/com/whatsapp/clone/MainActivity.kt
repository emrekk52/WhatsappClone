package com.whatsapp.clone

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHost
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.ActivityMainBinding
import com.whatsapp.clone.extensions.callShow
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.viewModel.ChatViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ChatViewModel
    lateinit var crud: firebase_CRUD
    var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WhatsappClone)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = findNavController(R.id.nav_host_fragment)
        val navGraph = navHostFragment.navInflater.inflate(R.navigation.nav_graph)

        navHostFragment.graph

        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        crud = firebase_CRUD()

        navGraph.startDestination = if (crud.getAuth()) R.id.nav_home else R.id.nav_accept
        navHostFragment.graph = navGraph
        checkCallReceiver()

    }


    private fun checkCallReceiver() {
        if (crud.auth.currentUser != null)
            crud.database.collection("call${crud.getCurrentId()}").document("call")
                .addSnapshotListener { value, error ->

                    if (value?.get("id").toString() != "null") {
                        val id = value?.get("id").toString()
                        val state = value?.get("call_state").toString()
                        getProfile(id,state)
                    } else
                        dialog?.dismiss()

                }
    }

    private fun getProfile(id: String,state:String) {
        crud.database.collection("users").document(id).addSnapshotListener { value, error ->

            val profile = MyProfileModel(
                value?.get("name").toString(),
                value?.get("profile_photo").toString(),
                value?.get("profile_id").toString(),
                value?.get("phone_number").toString(),
            )

                dialog = this.callShow(profile,state)

        }
    }


    override fun onResume() {
        super.onResume()
        if (crud.getAuth())
            viewModel.updateLastSeen(1)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (crud.getAuth())
            viewModel.updateLastSeen(2)

    }

    override fun onPause() {
        super.onPause()
        if (crud.getAuth())
            viewModel.updateLastSeen(2)

    }


}