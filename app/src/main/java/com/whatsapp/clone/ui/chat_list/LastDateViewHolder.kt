package com.whatsapp.clone.ui.chat_list

import agency.tango.android.avatarviewglide.GlideLoader
import android.graphics.Typeface
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import com.whatsapp.clone.R
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.ListItemChatCustomBinding
import com.whatsapp.clone.extensions.calcLastMessageDiff
import com.whatsapp.clone.extensions.upload
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.ui.home.HomeFragmentDirections
import com.whatsapp.clone.viewModel.ChatViewModel
import com.whatsapp.clone.viewModel.UnreadMessageCountViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LastDateViewHolder(
    v: ListItemChatCustomBinding,
    val find: NavController,
    val timestamp: Timestamp,
    val lastMessage: String,
    val message_read_status: Boolean,
    val sender_id: String,
    val image: String,
    val unreadCount: Int,
    val umcViewModel: UnreadMessageCountViewModel
) :
    RecyclerView.ViewHolder(v.root) {

    val date = timestamp.toDate().time


    private val lastDate: TextView by lazy { itemView.findViewById<TextView>(R.id.last_date) }
    private val last_message: TextView by lazy { itemView.findViewById<TextView>(R.id.chat_last_message) }


    init {
        checkDate()
        loadAvatar(v)
        goMessage(v)
        checkMessageRead(v)
    }

    private fun checkMessageRead(v: ListItemChatCustomBinding) {

        if (sender_id.equals(firebase_CRUD().getCurrentId())) {

            if (message_read_status)
                v.seenCheck.setImageResource(R.drawable.message_read)
            else
                v.seenCheck.setImageResource(R.drawable.message_unread)

            v.seenCheck.visibility = View.VISIBLE
        } else
            v.seenCheck.visibility = View.GONE


        if (!image.equals("")) {
            v.cameras.visibility = View.VISIBLE
            if (v.chatLastMessage.text.toString().equals(""))
                v.chatLastMessage.text = "fotoğraf"
        } else
            v.cameras.visibility = View.GONE

        if (v.chatLastMessage.text.toString().equals("Bu mesaj silindi")) {

            v.chatLastMessage.setTypeface(null, Typeface.ITALIC)
            v.chatLastMessage.text =
                Html.fromHtml("<u><font color='#808080'>Bu mesaj silindi</font></u>")

        }

        if (unreadCount > 0) {
            v.unreadMessageCount.text = "$unreadCount"
            v.unreadMessageCount.visibility = View.VISIBLE

            umcViewModel.count += unreadCount
            umcViewModel._count.postValue(umcViewModel.count)

        }


    }


    private fun checkDate() {
        last_message.text = lastMessage
        lastDate.text = calcLastMessageDiff(date)
    }

    private fun loadAvatar(v: ListItemChatCustomBinding) {
        v.avatarView.upload(v.chatlist?.profile_photo!!)
    }


    private fun goMessage(v: ListItemChatCustomBinding) {
        v.clRoot.setOnClickListener {
            find.navigate(
                HomeFragmentDirections.navHomeToChats(v.chatlist!!)
            )
        }

        v.avatarView.setOnClickListener {
            find.navigate(
                HomeFragmentDirections.actionNavHomeToUserProfileFragment(v.chatlist?.profile_id!!)
            )
        }
    }

}