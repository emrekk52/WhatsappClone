package com.whatsapp.clone.ui.chat_list

import agency.tango.android.avatarviewglide.GlideLoader
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.whatsapp.clone.R
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.FragmentChatListDesignBinding
import com.whatsapp.clone.databinding.ListItemChatCustomBinding
import com.whatsapp.clone.extensions.decryptWithAES
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.viewModel.ChatListViewModel
import com.whatsapp.clone.viewModel.ChatViewModel
import com.whatsapp.clone.viewModel.UnreadMessageCountViewModel


class ChatListFragment : Fragment() {

    lateinit var binding: FragmentChatListDesignBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChatListDesignBinding.inflate(layoutInflater)


        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = this

        val viewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)

        val umcViewModel: UnreadMessageCountViewModel by viewModels()


        viewModel.getChatList()
        viewModel.modelChat.observe(viewLifecycleOwner, {
            val adapter = ChatListAdapter(it, findNavController(), umcViewModel)
            binding.recylerView.adapter = adapter
        })

    }


}

class ChatListAdapter(
    val list: List<MyProfileModel>,
    val find: NavController,
    val umcViewModel: UnreadMessageCountViewModel
) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {


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
        getLastMessage(list[position], holder.view, find)
    }

    private fun getLastMessage(
        profile: MyProfileModel,
        holder: ListItemChatCustomBinding,
        find: NavController
    ) {


        var lastmessage = ""
        var sender_id = ""
        var message_send_time: Timestamp? = null
        var message_read_status: Boolean? = false
        var image: String? = ""
        val crud = firebase_CRUD()
        crud.database.collection("channel").orderBy("message_send_time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->

                var unreadCount = 0
                value?.documents?.forEach {

                    if (it["received_id"].toString()
                            .equals(crud.getCurrentId()) && it["sender_id"].toString()
                            .equals(profile.profile_id) ||
                        it["received_id"].toString()
                            .equals(profile.profile_id) && it["sender_id"].toString()
                            .equals(crud.getCurrentId())
                    ) {
                        lastmessage = it["message"].toString().decryptWithAES()!!
                        message_send_time = it["message_send_time"] as Timestamp
                        message_read_status = it["message_read_state"] as Boolean
                        sender_id = it["sender_id"].toString()
                        image = it["image_url"]?.toString()


                        if (it["received_id"].toString()
                                .equals(crud.getCurrentId()) && it["sender_id"].toString()
                                .equals(profile.profile_id) && message_read_status == false
                        )
                            unreadCount++


                        //return@forEach
                    }
                }


                LastDateViewHolder(
                    holder,
                    find,
                    message_send_time!!,
                    lastmessage,
                    message_read_status!!,
                    sender_id,
                    image!!, unreadCount,
                    umcViewModel
                )


            }


    }


    override fun getItemCount() = list.size

}