package com.whatsapp.clone.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.whatsapp.clone.R
import com.whatsapp.clone.databinding.FragmentZoomPhotoBinding


class ZoomPhotoFragment : Fragment() {

    val arg: ZoomPhotoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentZoomPhotoBinding.inflate(layoutInflater)
        Picasso.get().load(arg.imageUrl).fit().centerCrop().into(binding.zoomPhoto)


        return binding.root
    }




}