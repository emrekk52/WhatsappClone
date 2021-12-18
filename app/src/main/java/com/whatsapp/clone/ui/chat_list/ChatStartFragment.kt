package com.whatsapp.clone.ui.chat_list

import agency.tango.android.avatarviewglide.GlideLoader
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.whatsapp.clone.R
import com.whatsapp.clone.dao.ContactSynchronize
import com.whatsapp.clone.databinding.CustomContactBinding
import com.whatsapp.clone.databinding.FragmentChatStartBinding
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.extensions.upload
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.model.ProfileDatabase
import com.whatsapp.clone.viewModel.ChatStartViewModel


class ChatStartFragment : Fragment() {

    lateinit var binding: FragmentChatStartBinding
    private lateinit var viewModel: ChatStartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this).get(ChatStartViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChatStartBinding.inflate(layoutInflater)

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


        viewModel.list.observe(viewLifecycleOwner, {
            if (it.size == 0)
                readContacts()
            else {
                val adapter = ContactAdapter(it, findNavController())
                binding.contactRecycler.adapter = adapter
            }

        })


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            readContacts()
        else
            requireActivity().toastShow("İzin iptal edildi!")
    }


    private fun readContacts() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity?.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            activity?.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        else {
            val req =
                OneTimeWorkRequest.Builder(ContactSynchronize::class.java).addTag("contact_sync")
                    .build()
            WorkManager.getInstance()
                .enqueue(req)
        }


    }


}


class ContactAdapter(val list: List<ProfileDatabase>, val navController: NavController) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    class ViewHolder(val view: CustomContactBinding) : RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.custom_contact,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pr = MyProfileModel(
            list[position].name,
            list[position].profile_photo,
            list[position].profile_id,
            list[position].phone_number
        )
        holder.view.profile = pr

        holder.view.avatarView.upload(list[position].profile_photo)


        holder.view.clRoot.setOnClickListener {
            navController.navigate(
                ChatStartFragmentDirections.actionChatStartFragmentToNavChats(pr)
            )
        }


    }


    override fun getItemCount() = list.size

}





