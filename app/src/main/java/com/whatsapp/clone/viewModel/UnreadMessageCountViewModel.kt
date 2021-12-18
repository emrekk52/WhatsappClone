package com.whatsapp.clone.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UnreadMessageCountViewModel : ViewModel() {

    var count = 0

    val _count = MutableLiveData<Int>()


}