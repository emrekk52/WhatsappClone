package com.example.whatsappclone.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.whatsapp.clone.databinding.ViewMessageInputBinding
import com.whatsapp.clone.fragments.ChatsFragmentDirections
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.viewModel.ChatViewModel


/**
 * Basic message input view. Handles:
 * - Typing event
 * - Send message
 *
 * Doesn't handle more complex use cases like audio, video, file uploads, slash commands, editing text, threads or replies.
 * Stream's core API supports all of those though, so it's relatively easy to add
 *
 * When the user typed some text we change the microphone icon into a send button and hide the video button.
 */
class MessageInputView : ConstraintLayout {
    private lateinit var binding: ViewMessageInputBinding

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val inflater = LayoutInflater.from(context)
        binding = ViewMessageInputBinding.inflate(inflater, this, true)
    }

    fun setViewModel(
        viewModel: ChatViewModel,
        lifecycleOwner: LifecycleOwner?,
        navController: NavController,
        model: MyProfileModel
    ) {
        binding.lifecycleOwner = lifecycleOwner
        binding.viewModel = viewModel

        binding.voiceRecordingOrSend.setOnClickListener {
            if (!viewModel.messageInputText.value.toString().trim().equals("")) {
                viewModel.sendMessage(model)
                viewModel.messageInputText.value = ""
            }
        }

        binding.takePicture.setOnClickListener {
            navController.navigate(
                ChatsFragmentDirections.actionNavChatsToSendPhotoFragment(model.profile_id.toString())
            )
        }

        // listen to typing events and connect to the view model
        binding.messageInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                //  if (s.toString().isNotEmpty())
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (count > 0)
                    viewModel.updateLastSeen(3)
                else
                    viewModel.updateLastSeen(1)
            }
        })
    }

}