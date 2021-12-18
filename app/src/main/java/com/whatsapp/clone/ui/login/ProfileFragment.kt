package com.whatsapp.clone.ui.login

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.ktx.Firebase
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.FragmentProfileBinding
import com.whatsapp.clone.extensions.dialogShow
import com.whatsapp.clone.extensions.formatNumberToE164
import com.whatsapp.clone.extensions.hideKeyboard
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.viewModel.ProfileUpdateCRUD
import java.util.*


class ProfileFragment : Fragment() {


    val args: ProfileFragmentArgs by navArgs()
    lateinit var binding: FragmentProfileBinding
    var chooseURI: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)


        binding.profilePhoto.setOnClickListener {
            getChoosePicture()
        }


        binding.okButton.setOnClickListener {
            if (TextUtils.isEmpty(binding.editName.text))
                binding.edittextEmptyWarning.visibility = View.VISIBLE
            else
                profileUpdate()
        }


        binding.editName.doOnTextChanged { text, start, before, count ->

            if (text.toString() == " ")
                binding.editName.setText("")

            if (count > 0 && !TextUtils.isEmpty(text.toString().trim()))
                binding.edittextEmptyWarning.visibility = View.GONE

        }

        return binding.root
    }

    private fun profileUpdate() {

        requireActivity().hideKeyboard()

        val crud = firebase_CRUD()
        val dialog = requireActivity().dialogShow("Profil kaydediliyor..").also { it.show() }
        val viewModel: ProfileUpdateCRUD by viewModels()
        val profile = MyProfileModel(binding.editName.text.toString())
        profile.phone_number = formatNumberToE164(args.phone, Locale.getDefault().country)


        crud.uploadImage(
            profile,
            chooseURI,
            viewModel,
            dialog,
            findNavController(),
            requireContext()
        )


    }


    private fun getChoosePicture() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                activity?.requestPermissions(permissions, PERMISSION_CODE)
            } else
                chooseImageGallery()

        } else
            chooseImageGallery()

    }

    private fun chooseImageGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    chooseImageGallery()
                else
                    activity?.toastShow("İzin reddedildi!")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            binding.profilePhoto.setImageURI(data?.data)
            chooseURI = data?.data!!
        }


    }


    companion object {
        private val PERMISSION_CODE = 1001
        private val IMAGE_CHOOSE = 1000
    }

}