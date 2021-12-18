package com.whatsapp.clone.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.whatsapp.clone.databinding.FragmentVerifiedPhoneBinding

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import android.app.Dialog
import android.text.Editable
import android.text.TextWatcher
import androidx.navigation.fragment.findNavController
import com.whatsapp.clone.extensions.dialogEditShow
import com.whatsapp.clone.extensions.dialogShow
import com.whatsapp.clone.extensions.toastShow


class VerifiedPhoneFragment : Fragment() {

    lateinit var binding: FragmentVerifiedPhoneBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerifiedPhoneBinding.inflate(layoutInflater)

        binding.ccp.registerCarrierNumberEditText(binding.editPhoneNumber)

        binding.editPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == " ")
                    binding.editPhoneNumber.text?.clear().also {
                        binding.okButton.isEnabled = false
                    }
                else if (count > 0)
                    binding.okButton.isEnabled = true
                else if (count==0)
                    binding.okButton.isEnabled = false

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.okButton.setOnClickListener {
            requireActivity().dialogEditShow(binding.ccp.formattedFullNumber, findNavController())
        }

        return binding.root
    }


}