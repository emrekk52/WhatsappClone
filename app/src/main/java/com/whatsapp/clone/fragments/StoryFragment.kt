package com.whatsapp.clone.fragments

import agency.tango.android.avatarviewglide.GlideLoader
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import com.whatsapp.clone.R
import com.whatsapp.clone.databinding.FragmentStoryBinding
import com.whatsapp.clone.databinding.ListStoryItemCustomBinding
import com.whatsapp.clone.extensions.calcStoryTime
import com.whatsapp.clone.extensions.dialogShow
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.extensions.upload
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.model.StoryListModel
import com.whatsapp.clone.model.StoryModel
import com.whatsapp.clone.ui.home.HomeFragmentDirections
import com.whatsapp.clone.viewModel.StoryViewModel
import java.lang.Exception


class StoryFragment : Fragment() {


    private lateinit var binding: FragmentStoryBinding
    private lateinit var viewModel: StoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoryBinding.inflate(layoutInflater)

        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var list: Array<StoryModel>? = null
        var profile: MyProfileModel? = null

        viewModel = ViewModelProvider(this).get(StoryViewModel::class.java)
        viewModel.my_profile.observe(viewLifecycleOwner, {
            binding.myPhoto.upload(it.profile_photo!!)
            profile = it
        })
        viewModel.myStory.observe(viewLifecycleOwner, {
            if (it.size > 0) {
                list = it.toTypedArray()
                Picasso.get().load(it[it.size - 1].story_url).fit().centerCrop()
                    .into(binding.myPhoto)
                binding.stText.text = "Durumu görmek için dokun"
                binding.circularStatusView.setPortionsCount(it.size)
                binding.circularStatusView.visibility = View.VISIBLE
            } else {
                binding.stText.text = "Durum güncellemesi eklemek için dokun"
                binding.circularStatusView.visibility = View.GONE
            }
        })

        viewModel.profile.observe(viewLifecycleOwner, {
            viewModel.getOtherStories(it)
        })

        viewModel.stories.observe(viewLifecycleOwner, {
            val adapter = StoryAdapter(it, findNavController())
            binding.storyRecycler.adapter = adapter
        })


        binding.relativeLayout.setOnClickListener {
            getChoosePicture()
        }


        binding.myPhoto.setOnClickListener {
            if (list != null && list?.size!! > 0 && profile != null)
                findNavController().navigate(
                    HomeFragmentDirections.actionNavHomeToStoryDetailFragment(
                        StoryListModel(
                            profile,
                            list
                        )
                    )
                )
        }


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
            val chooseURI = data?.data!!
            val dialog = requireActivity().dialogShow("Hikaye yükleniyor..").also { it.show() }
            viewModel.uploadStory(chooseURI, requireContext(), dialog)
        }


    }


    companion object {
        val PERMISSION_CODE = 1001
        val IMAGE_CHOOSE = 1000
    }


}


class StoryAdapter(val list: List<StoryListModel>, val nav: NavController) :
    RecyclerView.Adapter<StoryAdapter.StoryHolder>() {


    class StoryHolder(val view: ListStoryItemCustomBinding) : RecyclerView.ViewHolder(view.root) {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHolder {
        return StoryHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_story_item_custom,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StoryHolder, position: Int) {
        holder.view.storymodel = list[position]


        holder.view.avPhoto.upload(list[position].model?.profile_photo!!)


        val size = list[position].storyList?.size!!
        holder.view.circularStatusView.setPortionsCount(size)

        holder.view.storyTime.text =
            calcStoryTime(list[position].storyList!![size - 1].story_time?.toDate()?.time!!)

        holder.view.clicklable.setOnClickListener {
            nav.navigate(
                HomeFragmentDirections.actionNavHomeToStoryDetailFragment(list[position])
            )
        }
    }

    override fun getItemCount() = list.size
}