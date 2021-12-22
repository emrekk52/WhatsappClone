package com.whatsapp.clone.ui.home

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.whatsapp.clone.R
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.extensions.animateFab
import com.whatsapp.clone.extensions.dialogShow
import com.whatsapp.clone.extensions.toastShow
import com.whatsapp.clone.fragments.CallFragment
import com.whatsapp.clone.fragments.StoryFragment
import com.whatsapp.clone.ui.EmptyFragment
import com.whatsapp.clone.ui.chat_list.ChatListFragment
import com.whatsapp.clone.viewModel.StoryViewModel

private const val INDEX_OF_CAMERA = 0
private const val INDEX_OF_CHATS = 1
private const val INDEX_OF_STATUS = 2
private const val INDEX_OF_CALLS = 3
private const val NUM_TABS = 4
private val TAB_TITLES = mapOf(
    INDEX_OF_CAMERA to "kamera",
    INDEX_OF_CHATS to "sohbetler",
    INDEX_OF_STATUS to "durum",
    INDEX_OF_CALLS to "aramalar"
)

class TabsAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = NUM_TABS
    override fun createFragment(position: Int): Fragment = when (position) {

        INDEX_OF_CHATS -> {
            ChatListFragment()
        }

        INDEX_OF_STATUS -> StoryFragment()

        INDEX_OF_CALLS->CallFragment()


        else -> EmptyFragment.newInstance(TAB_TITLES[position] ?: "")


    }
}

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.myProfile) {
            findNavController().navigate(
                HomeFragmentDirections.actionNavHomeToUserProfileFragment(firebase_CRUD().getCurrentId())
            )
            return true
        }
        return false
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity: AppCompatActivity = activity as AppCompatActivity
        val fragmentView =
            requireNotNull(view) { "View should not be null when calling onActivityCreated" }

        val toolbar: Toolbar = fragmentView.findViewById(R.id.toolbar)
        activity.setSupportActionBar(toolbar)

        val tabLayout: TabLayout = fragmentView.findViewById(R.id.tabLayout)
        viewPager = fragmentView.findViewById(R.id.view_pager)
        viewPager.adapter = TabsAdapter(childFragmentManager, lifecycle)

        val fab: FloatingActionButton = fragmentView.findViewById(R.id.fabButton)


        fab.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionNavHomeToChatStartFragment()
            )
        }








        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            if (position == INDEX_OF_CAMERA) {
                tab.setIcon(R.drawable.camera_icon)
                val colors =
                    ResourcesCompat.getColorStateList(resources, R.color.tab_icon, activity.theme)
                tab.icon?.apply { DrawableCompat.setTintList(DrawableCompat.wrap(this), colors) }
            } else {
                tab.text = TAB_TITLES[position]
            }


            viewPager.setCurrentItem(tab.position, true)
        }.attach()
        tabLayout.getTabAt(INDEX_OF_CHATS)?.let {
            tabLayout.selectTab(it)
            tabLayout.setTabWidthAsWrapContent(0)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 2)
                    tab.removeBadge()
                if (tab?.position == 1)
                    tab.removeBadge()

                fab.animateFab(tab?.position!!)

                when (tab?.position) {

                    1 -> fab.setOnClickListener {
                        findNavController().navigate(
                            HomeFragmentDirections.actionNavHomeToChatStartFragment()
                        )
                    }

                    2 -> fab.setOnClickListener {
                        getChoosePicture()
                    }

                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 2)
                    tab.removeBadge()
                if (tab?.position == 1)
                    tab.removeBadge()


            }
        })


    }

    fun TabLayout.setTabWidthAsWrapContent(tabPosition: Int) {
        val layout = (this.getChildAt(0) as LinearLayout).getChildAt(tabPosition) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0f
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        layout.layoutParams = layoutParams
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
            val dialog = requireActivity().dialogShow("Hikaye yükleniyor..").also { it.show() }
            val viewModel: StoryViewModel by viewModels()
            viewModel.uploadStory(chooseURI, requireContext(), dialog)
        }


    }


}