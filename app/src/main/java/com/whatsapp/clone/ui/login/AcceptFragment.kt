package com.whatsapp.clone.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.whatsapp.clone.R
import com.whatsapp.clone.databinding.FragmentAcceptBinding


class AcceptFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAcceptBinding.inflate(layoutInflater)

        binding.okButton.setOnClickListener {
            findNavController().navigate(
                AcceptFragmentDirections.actionNavAcceptToVerifiedPhoneFragment()
            )
        }

        return binding.root
    }


}