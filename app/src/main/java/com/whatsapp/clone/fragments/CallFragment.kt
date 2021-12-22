package com.whatsapp.clone.fragments

import android.app.DownloadManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.whatsapp.clone.R
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.CallItemBinding
import com.whatsapp.clone.databinding.FragmentCallBinding
import com.whatsapp.clone.extensions.calcStoryTime
import com.whatsapp.clone.extensions.upload
import com.whatsapp.clone.model.CallModel


class CallFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentCallBinding.inflate(layoutInflater)

        val viewModel: CallViewModel by viewModels()

        viewModel._list.observe(viewLifecycleOwner, {
            val adapter = CallAdapter(it)
            binding.recyclerView.adapter = adapter
        })


        return binding.root
    }


}

class CallViewModel : ViewModel() {


    val _list = MutableLiveData<List<CallModel>>()
    val crud: firebase_CRUD

    init {
        crud = firebase_CRUD()
        getCall()
    }


    private fun getCall() {
        crud.database.collection("mycalls${crud.getCurrentId()}")
            .orderBy("call_time",Query.Direction.DESCENDING).addSnapshotListener { value, error ->

                val list = mutableListOf<CallModel>()
                if (error == null)
                    value?.documents?.forEach {
                        val call_model = it.toObject(CallModel::class.java)
                        if (call_model != null) {
                            list.add(call_model)
                        }

                    }

                _list.value = list


            }
    }

}


class CallAdapter(val list: List<CallModel>) : RecyclerView.Adapter<CallAdapter.CallHolder>() {


    class CallHolder(val view: CallItemBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallAdapter.CallHolder {
        return CallHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.call_item,
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: CallAdapter.CallHolder, position: Int) {
        holder.view.call = list[position]

        if (list[position].call_id == 1) {
            holder.view.callTypeView.setImageResource(R.drawable.call_outgoing)
        } else if (list[position].call_id == 2) {
            holder.view.callTypeView.setImageResource(R.drawable.call_missed)
        }


        holder.view.time.text= calcStoryTime(list[position].call_time?.toDate()?.time!!)

    }

    override fun getItemCount() = list.size


}