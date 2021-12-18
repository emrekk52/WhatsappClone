package com.whatsapp.clone.fragments

import agency.tango.android.avatarviewglide.GlideLoader
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.whatsapp.clone.R
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.CustomContactBinding
import com.whatsapp.clone.databinding.FragmentSeenMyStoryBinding
import com.whatsapp.clone.databinding.ListItemChatCustomBinding
import com.whatsapp.clone.extensions.dialogShow
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.model.ProfileDatabase
import com.whatsapp.clone.ui.chat_list.ChatStartFragmentDirections
import com.whatsapp.clone.ui.chat_list.ContactAdapter

class SeenMyStoryFragment : Fragment() {

    val arg: SeenMyStoryFragmentArgs by navArgs()

    private lateinit var binding: FragmentSeenMyStoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSeenMyStoryBinding.inflate(layoutInflater)

        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val adapter = SeenAdapter(arg.profile.toList())
        binding.seenRecycler.adapter = adapter


        binding.deleteStory.setOnClickListener {
            alertDelete()
        }


    }

    private fun alertDelete() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("Bu hikayeyi gerçekten silmek istiyor musun?")
            .setPositiveButton("Evet", DialogInterface.OnClickListener { dialog, id ->
                deleteStory()
            }).setNegativeButton("Hayır", DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
            })
        builder.create()
        builder.show()

    }

    private fun deleteStory() {

        val dialog = requireActivity().dialogShow("Hikaye siliniyor..").also {
            it.show()
        }

        val crud = firebase_CRUD()
        crud.database.collection("story${crud.getCurrentId()}").document(arg.id).delete()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    dialog.dismiss()
                    requireContext().toastShow("Hikaye silindi!")
                    findNavController().navigateUp()
                } else
                    requireContext().toastShow(it.exception?.message.toString())
            }

    }


}


class SeenAdapter(val list: List<MyProfileModel>) :
    RecyclerView.Adapter<SeenAdapter.ViewHolder>() {

    class ViewHolder(val view: ListItemChatCustomBinding) : RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_chat_custom,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.view.chatlist = list[position]
        GlideLoader().loadImage(
            holder.view.avatarView,
            list[position].profile_photo,
            list[position].name
        )


        holder.view.chatLastMessage.text = list[position].status


    }


    override fun getItemCount() = list.size

}
