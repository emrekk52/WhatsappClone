package com.whatsapp.clone

import android.annotation.SuppressLint
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
import com.whatsapp.clone.viewModel.ChatViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ChatViewModel
    lateinit var crud: firebase_CRUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = findNavController(R.id.nav_host_fragment)
        val navGraph = navHostFragment.navInflater.inflate(R.navigation.nav_graph)

        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        crud = firebase_CRUD()

        navGraph.startDestination = if (crud.getAuth()) R.id.nav_home else R.id.nav_accept
        navHostFragment.graph = navGraph

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