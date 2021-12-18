package com.whatsapp.clone.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.whatsapp.clone.R
import com.whatsapp.clone.databinding.FragmentSendPhotoBinding
import com.whatsapp.clone.extensions.dialogShow
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.viewModel.ChatViewModel


class SendPhotoFragment : Fragment() {

    val arg: SendPhotoFragmentArgs by navArgs()

    private lateinit var binding: FragmentSendPhotoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSendPhotoBinding.inflate(layoutInflater)

        getChoosePicture()

        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }


    private fun chooseImageGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, StoryFragment.IMAGE_CHOOSE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            StoryFragment.PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    chooseImageGallery()
                else
                    activity?.toastShow("İzin reddedildi!")
            }
        }
    }

    private fun getChoosePicture() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                activity?.requestPermissions(permissions, StoryFragment.PERMISSION_CODE)
            } else
                chooseImageGallery()

        } else
            chooseImageGallery()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == StoryFragment.IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            val chooseURI = data?.data!!
            binding.sendPhoto.setImageURI(chooseURI)
            binding.voiceRecordingOrSend.setOnClickListener {
                val viewModel: ChatViewModel by viewModels()
                val dialog = requireActivity().dialogShow("Fotoğraf gönderiliyor..").also {
                    it.show()
                }
                viewModel.sendMessage(
                    binding.messageInput.text.toString(),
                    arg.profileid,
                    chooseURI,
                    requireContext(),
                    findNavController(),
                    dialog
                )
            }
        } else
            findNavController().navigateUp()


    }


}