package com.whatsapp.clone.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.whatsapp.clone.R
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.FragmentUserProfileBinding
import com.whatsapp.clone.extensions.*
import com.whatsapp.clone.fragments.StoryFragment
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.model.StoryModel
import com.whatsapp.clone.viewModel.ProfileViewModel
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    val arg: UserProfileFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(layoutInflater)

        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity: AppCompatActivity = activity as AppCompatActivity

        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        val viewModel: ProfileViewModel by viewModels()

        viewModel.getProfile(arg.profileid)

        viewModel.profile.observe(viewLifecycleOwner, {

            if (it.profile_id?.equals(firebase_CRUD().getCurrentId())!!)
                updateMyProfile(it)
            else
                updateProfile(it)


        })


    }


    private fun updateProfile(it: MyProfileModel) {
        binding.collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
        binding.collapsingToolbarLayout.setExpandedTitleColor(Color.parseColor("#ECEAEA"))
        binding.collapsingToolbarLayout.title = it.name
        binding.roaming.text = when (it.roaming?.trim()) {

            "çevrimiçi" -> it.roaming
            "yazıyor.." -> "çevrimiçi"
            else -> calcDay(it.roaming?.toLong()!!)
        }
        binding.status.text = it.status
        binding.phone.text = it.phone_number
        binding.registrDate.text = calcRegDate(it.registration_date?.toDate()?.time!!)
        binding.block.text = "${it.name} kişisini engelle"

        Picasso.get().load(it.profile_photo).fit().centerCrop().into(binding.avatar)
    }

    private fun updateMyProfile(model: MyProfileModel) {
        binding.collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
        binding.collapsingToolbarLayout.setExpandedTitleColor(Color.parseColor("#ECEAEA"))
        binding.collapsingToolbarLayout.title = "Ben"
        binding.roaming.visibility = View.GONE
        binding.editStatus.visibility = View.VISIBLE
        binding.nameLayout.visibility = View.VISIBLE
        binding.photoChange.visibility = View.VISIBLE
        binding.blockLayout.visibility = View.GONE


        binding.stText.text = "Hakkımda"
        binding.phText.text = "Telefon numaram"
        binding.rgText.text = "Katıldığım tarih"


        binding.status.text = model.status
        binding.name.text = model.name
        binding.phone.text = model.phone_number
        binding.registrDate.text = calcRegDate(model.registration_date?.toDate()?.time!!)

        Picasso.get().load(model.profile_photo).fit().centerCrop().into(binding.avatar)


        binding.editStatus.setOnClickListener {
            openEditFragment(1, model.status!!)
        }

        binding.editName.setOnClickListener {
            openEditFragment(0, model.name!!)
        }

        binding.photoChange.setOnClickListener {
            getChoosePicture()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == StoryFragment.IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            val chooseURI = data?.data!!
            val dialog =
                requireActivity().dialogShow("Profil resmi güncelleniyor..").also { it.show() }
            changeProfilePhoto(chooseURI, dialog)

        }


    }

    fun changeProfilePhoto(photo_uri: Uri, alertDialog: AlertDialog) {
        val crud = firebase_CRUD()
        val ref = crud.storage.child("user_photo/${UUID.randomUUID()}}")
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photo_uri)
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
                    crud.database.collection("users").document(arg.profileid)
                        .update("profile_photo", it).addOnCompleteListener {
                            it.exception?.let {
                                requireContext().toastShow(it.message.toString())
                            }
                            alertDialog.dismiss()
                            requireContext().toastShow("Profil resmi değiştirildi!")
                        }


                }

        }.addOnFailureListener {
            requireContext().toastShow(it.message.toString())
            alertDialog.dismiss()
        }.addOnCanceledListener {
            alertDialog.dismiss()
        }


    }

    private fun openEditFragment(id: Int, t: String) {
        val dialog = BottomSheetDialog(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.edit_dialog_profile, null)

        val edit_name = view.findViewById<TextView>(R.id.edit_name)
        val edit_text = view.findViewById<TextInputEditText>(R.id.edittext)
        val okey = view.findViewById<TextView>(R.id.okey)


        edit_text.requestFocus()


        when (id) {

            0 -> edit_name.text = "Ad".also {
                edit_text.hint = "adınızı yazın.."
                edit_text.setText(t)

            }
            else -> edit_name.text =
                "Hakkımda".also {
                    edit_text.hint = "hakkında durumunu yazın.."
                    edit_text.setText(t)
                }
        }

        val crud = firebase_CRUD()
        when (id) {
            0 -> okey.setOnClickListener { v ->
                crud.database.collection("users").document(arg.profileid)
                    .update("name", edit_text.text.toString()).addOnCompleteListener {
                        it.exception?.let {
                            requireContext().toastShow(it.message.toString())
                        }
                        dialog.dismiss()
                    }
            }
            else -> okey.setOnClickListener { v ->
                crud.database.collection("users").document(arg.profileid)
                    .update("status", edit_text.text.toString()).addOnCompleteListener {
                        it.exception?.let {
                            requireContext().toastShow(it.message.toString())
                        }
                        dialog.dismiss()
                    }
            }
        }

        dialog.setContentView(view)

        dialog.setOnDismissListener {
            requireActivity().hideKeyboard()
            requireContext().hideKeyboard(view)

        }

        dialog.show()

        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_channel, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return false
    }


}