package com.whatsapp.clone.fragments

import agency.tango.android.avatarviewglide.GlideLoader
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.whatsapp.clone.R
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.FragmentChatsBinding
import com.whatsapp.clone.databinding.FromChatItemBinding
import com.whatsapp.clone.model.Message
import com.whatsapp.clone.ui.home.HomeFragmentDirections
import com.whatsapp.clone.viewModel.ChatViewModel
import com.whatsapp.clone.model.MyProfileModel
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.text.Html
import android.util.Base64
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList
import android.widget.Toast

import android.app.ListActivity
import android.graphics.Typeface
import android.media.MediaPlayer
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras

import androidx.recyclerview.widget.ItemTouchHelper
import com.whatsapp.clone.extensions.*


class ChatsFragment : Fragment() {

    private val args: ChatsFragmentArgs by navArgs()
    private lateinit var binding: FragmentChatsBinding
    lateinit var adapter: MessageListAdapter
    lateinit var viewModel: ChatViewModel
    var list = arrayListOf<Message>()


    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {


            return false
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {

            val diff =
                Timestamp.now().seconds - list[viewHolder.adapterPosition].message_send_time!!.seconds

            if (diff <= 10L)
                firebase_CRUD().database.collection("channel")
                    .document(list[viewHolder.adapterPosition].message_id)
                    .update("message", "Bu mesaj silindi".encrypt())
            else if (list[viewHolder.adapterPosition].message.decryptWithAES()
                    .equals("Bu mesaj silindi")
            ) {
                adapter.notifyDataSetChanged()
            } else {
                context?.toastShow("Mesaj gönderildikten 10 saniye içerisinde silinebilir!")
                adapter.notifyDataSetChanged()
            }

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats, container, false)
        return binding.root
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


    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val activity: AppCompatActivity = activity as AppCompatActivity

        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }



        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)


        binding.messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE)
                    binding.scrollDown.visibility = View.GONE
                else
                    binding.scrollDown.visibility = View.VISIBLE

            }
        })

        binding.scrollDown.setOnClickListener {
            binding.messageRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }


        binding.profileLayout.setOnClickListener {
            findNavController().navigate(
                ChatsFragmentDirections.actionNavChatsToUserProfileFragment(args.model.profile_id!!)
            )
        }


        binding.viewModel = viewModel
        binding.messageInputView.setViewModel(
            viewModel,
            this,
            findNavController(),
            args.model
        )



        binding.avatarGroup.upload(args.model.profile_photo.toString())



        seenMessage(args.model.profile_id!!)

        binding.userName.text = args.model.name

        var st = false

        checkLastSeen()


        viewModel.getMessage(args.model.profile_id!!)
        viewModel.chat_list.observe(viewLifecycleOwner, {

            it?.let {
                if (it.size > 0)
                    if (!st) {
                        list = it as ArrayList<Message>
                        adapter = MessageListAdapter(
                            list,
                            firebase_CRUD().getCurrentId(),
                            findNavController()
                        )
                        binding.messageRecyclerView.adapter = adapter
                        smoothToScroll(adapter)
                        binding.messageRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                        st = true
                    } else if (list.size == it.size) {
                        adapter.notifyDataSetChanged()
                    } else {
                        list.add(it[it.size - 1])
                        adapter.notifyItemInserted(list.size - 1)
                        binding.messageRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)

                        // if (!it[it.size - 1].sender_id.equals(firebase_CRUD().getCurrentId()))
                        val media = MediaPlayer.create(requireContext(), R.raw.m_sound)
                        media.setOnPreparedListener {
                            println("hazırım laa")
                        }
                        println("gedim hemen")
                        media.start()


                    }


            }


        })

        val touchHelper = ItemTouchHelper(simpleItemTouchCallback)
        touchHelper.attachToRecyclerView(binding.messageRecyclerView)


    }


    private fun smoothToScroll(adapter: MessageListAdapter) {
        if (Build.VERSION.SDK_INT >= 11) {
            binding.messageRecyclerView.addOnLayoutChangeListener(object :
                View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int, top: Int, right: Int, bottom: Int,
                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
                ) {
                    if (bottom < oldBottom) {
                        binding.messageRecyclerView.postDelayed(Runnable {
                            binding.messageRecyclerView.smoothScrollToPosition(
                                adapter.itemCount - 1
                            )
                        }, 100)
                    }
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        this.viewModelStore.clear()
    }

    private fun checkLastSeen() {

        viewModel.checkLastSeen(args.model.profile_id!!)
        viewModel.seen.observe(viewLifecycleOwner, {

            it?.let {

                args.model.roaming = it

                when (it) {

                    "çevrimiçi" -> binding.statusRoaming.text = it
                    "yazıyor.." -> binding.statusRoaming.text = it
                    else -> {
                        val timestamp = it.toLong()

                        binding.statusRoaming.text = calcDay(timestamp)

                    }
                }

                binding.statusRoaming.visibility = View.VISIBLE
                binding.statusRoaming.isSelected = true
            }

        })

    }


    fun seenMessage(user_id: String) {

        val crud = firebase_CRUD()
        crud.database.collection("channel").addSnapshotListener { value, error ->


            error?.let {

                value?.documents?.forEach {


                    if (it["received_id"].toString()
                            .equals(crud.getCurrentId()) && it["sender_id"].toString()
                            .equals(user_id)
                    ) {

                        crud.database.collection("channel").document(it.id)
                            .update("message_read_state", true)

                    }


                }

            }

        }


    }


}


class MessageListAdapter(val list: List<Message>, val current_id: String, val nav: NavController) :
    RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {

    val VIEW_TYPE_MESSAGE_SENT = 1
    val VIEW_TYPE_MESSAGE_RECEIVED = 2
    val VIEW_TYPE_DATE = 3


    class ViewHolder(val view: View, val nav: NavController) : RecyclerView.ViewHolder(view) {

        val todayDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val format_t = SimpleDateFormat("dd EE yyyy", Locale.getDefault())


        fun sentHolder(message: Message) {

            val messageText: TextView by lazy { itemView.findViewById(R.id.message_me) }
            val sendTimeText: TextView by lazy { itemView.findViewById(R.id.text_send_time) }
            val message_unread_or_read: ImageView by lazy { itemView.findViewById(R.id.message_unread) }
            val frame: CardView by lazy { itemView.findViewById(R.id.photo_frame) }
            val image: ImageView by lazy { itemView.findViewById(R.id.photo) }

            messageText.setTypeface(null, Typeface.NORMAL)


            if (!message.message.decryptWithAES().equals("")) {
                if (message.message.decryptWithAES().equals("Bu mesaj silindi")) {
                    messageText.setTypeface(null, Typeface.ITALIC)
                    val mesaj = message.message.decryptWithAES()
                    messageText.text =
                        Html.fromHtml("<u><font color='#808080'>${mesaj}</font></u>")
                } else
                    messageText.text = message.message.decryptWithAES()
            } else
                messageText.visibility = View.GONE


            message_unread_or_read.setImageResource(
                if (message.message_read_state == true)
                    R.drawable.message_read
                else
                    R.drawable.message_unread
            )

            when (message.image_url) {

                "" -> frame.visibility = View.GONE
                else -> {
                    Picasso.get().load(message.image_url).fit().centerCrop().into(image)
                    frame.visibility = View.VISIBLE
                }

            }

            image.setOnClickListener {
                nav.navigate(
                    ChatsFragmentDirections.actionNavChatsToZoomPhotoFragment(message.image_url!!)
                )
            }


            sendTimeText.text = todayDateFormat.format(message.message_send_time?.toDate()?.time)

        }


        fun receivedHolder(message: Message) {

            val messageText: TextView by lazy { itemView.findViewById(R.id.text_message_other) }
            val receivedTimeText: TextView by lazy { itemView.findViewById(R.id.text_timestamp_other) }
            val frame: CardView by lazy { itemView.findViewById(R.id.photo_frame) }
            val image: ImageView by lazy { itemView.findViewById(R.id.photo) }

            messageText.setTypeface(null, Typeface.NORMAL)


            if (!message.message.decryptWithAES().equals("")) {
                if (message.message.decryptWithAES().equals("Bu mesaj silindi")) {
                    messageText.setTypeface(null, Typeface.ITALIC)
                    messageText.text =
                        Html.fromHtml("<u><font color='#808080'>Bu mesaj silindi</font></u>")

                } else
                    messageText.text = message.message.decryptWithAES()
            } else
                messageText.visibility = View.GONE


            receivedTimeText.text =
                todayDateFormat.format(message.message_send_time?.toDate()?.time)

            when (message.image_url) {

                "" -> frame.visibility = View.GONE
                else -> {
                    Picasso.get().load(message.image_url).fit().centerCrop().into(image)
                    frame.visibility = View.VISIBLE
                }

            }

            image.setOnClickListener {
                nav.navigate(
                    ChatsFragmentDirections.actionNavChatsToZoomPhotoFragment(message.image_url!!)
                )
            }

        }


        fun seperate(timestamp: Timestamp) {

            val date_time: TextView by lazy { itemView.findViewById(R.id.date_seperate_text) }

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp.toDate().time
            val day = calendar.get(Calendar.DAY_OF_MONTH)


            if (getCurrentDay() == day) {
                date_time.text = "Bugün"
            } else if (getCurrentDay() - 1 == day) {
                date_time.text = "Dün"
            } else
                date_time.text = format_t.format(timestamp.toDate().time)


        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return if (viewType == VIEW_TYPE_MESSAGE_SENT) ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.from_chat_item, parent, false), nav
        ) else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED)
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.to_chat_item, parent, false),
                nav
            )
        else
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.date_seperator, parent, false),
                nav
            )


    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].sender_id.equals(current_id)) VIEW_TYPE_MESSAGE_SENT else if (list[position].sender_id.equals(
                ""
            ) &&
            list[position].received_id.equals("")
        )
            VIEW_TYPE_DATE else VIEW_TYPE_MESSAGE_RECEIVED

    }


    override fun onBindViewHolder(holder: MessageListAdapter.ViewHolder, position: Int) {


        when (holder.itemViewType) {

            VIEW_TYPE_MESSAGE_SENT -> {
                holder.sentHolder(list[position])
            }

            VIEW_TYPE_MESSAGE_RECEIVED -> {
                holder.receivedHolder(list[position])
            }

            else -> holder.seperate(list[position].message_send_time!!)

        }


    }

    override fun getItemCount() = list.size


}


