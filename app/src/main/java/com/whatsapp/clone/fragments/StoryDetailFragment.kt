package com.whatsapp.clone.fragments

import agency.tango.android.avatarviewglide.GlideLoader
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.whatsapp.clone.databinding.FragmentStoryDetailBinding
import jp.shts.android.storiesprogressview.StoriesProgressView
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.navigation.fragment.findNavController
import android.R
import com.squareup.picasso.Callback
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.extensions.calcDay
import com.whatsapp.clone.extensions.calcStoryTime
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.extensions.upload
import java.lang.Exception


class StoryDetailFragment : Fragment(), StoriesProgressView.StoriesListener {

    private lateinit var binding: FragmentStoryDetailBinding
    private val args: StoryDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentStoryDetailBinding.inflate(layoutInflater)

        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        )


    }


    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        requireActivity().window.clearFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        requireActivity().window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        requireActivity().window.clearFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        requireActivity().window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        requireActivity().window.clearFlags(View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


    var pressTime = 0L
    var limit = 500L

    private val onTouchListener = OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                binding.storiesView.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                binding.storiesView.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }


    var counter = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.storiesView.setStoriesCount(args.storymodel.storyList!!.size)
        binding.storiesView.setStoryDuration(5000L)
        binding.storiesView.setStoriesListener(this)

        load(counter)



        binding.name.text =
            if (args.storymodel.model?.profile_id.equals(firebase_CRUD().getCurrentId())) "Ben" else args.storymodel.model?.name


        binding.profilePhoto.upload(
            args.storymodel.model?.profile_photo!!
        )



        binding.reverse.setOnClickListener { binding.storiesView.reverse() }
        binding.reverse.setOnTouchListener(onTouchListener)


        binding.skip.setOnClickListener { binding.storiesView.skip() }
        binding.skip.setOnTouchListener(onTouchListener)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.seenCount.setOnClickListener {
            if (args.storymodel.storyList!![counter].seenProfile != null)
                findNavController().navigate(
                    StoryDetailFragmentDirections.actionStoryDetailFragmentToSeenMyStoryFragment(
                        args.storymodel.storyList!![counter].seenProfile?.toTypedArray()!!,
                        args.storymodel.storyList!![counter].id.toString()
                    )
                )
        }


    }

    fun load(counter: Int) {
        if (counter < args.storymodel.storyList!!.size) {
            seenStory(args.storymodel.storyList!!.get(counter).id!!)
            seenCounter()
            binding.storiesView.pause()
            binding.time.text =
                calcStoryTime(args.storymodel.storyList!!.get(counter).story_time?.toDate()?.time!!)
            Picasso.get().load(args.storymodel.storyList!![counter].story_url)
                .into(binding.image, object : Callback {
                    override fun onSuccess() {
                        if (counter == 0)
                            binding.storiesView.startStories()
                        else
                            binding.storiesView.resume()
                    }

                    override fun onError(e: Exception?) {
                        requireContext().toastShow("Resim yüklenemedi! -> ${e?.localizedMessage.toString()}")
                    }

                })
        }
    }

    private fun seenCounter() {
        if (args.storymodel.model?.profile_id!!.equals(firebase_CRUD().getCurrentId()))
            binding.seenCount.text =
                if (args.storymodel.storyList!!.get(counter).seenProfile.toString() == "null") "0" else
                    args.storymodel.storyList!!.get(counter).seenProfile?.size.toString()


    }

    private fun seenStory(id: String) {
        val crud = firebase_CRUD()
        if (!args.storymodel.model?.profile_id.equals(crud.getCurrentId())) {
            val map = HashMap<String, Any>()
            map.put("id", crud.getCurrentId())
            crud.database.collection("seen${id}").document(crud.getCurrentId())
                .set(map)
        }

    }

    override fun onNext() {
        load(++counter)
    }

    override fun onPrev() {
        if (counter - 1 < 0)
            return
        load(--counter)
    }

    override fun onComplete() {
        findNavController().navigateUp()
    }

    override fun onDestroy() {
        binding.storiesView.destroy()
        super.onDestroy()
    }


}