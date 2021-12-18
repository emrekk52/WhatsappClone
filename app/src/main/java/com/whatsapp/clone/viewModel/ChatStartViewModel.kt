package com.whatsapp.clone.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.whatsapp.clone.dao.ContactRepository
import com.whatsapp.clone.dao.ContactRoomDatabase
import com.whatsapp.clone.dao.ContactDao
import com.whatsapp.clone.model.ProfileDatabase

class ChatStartViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: ContactDao
    private val repo: ContactRepository
    val list: LiveData<List<ProfileDatabase>>

    init {
        dao = ContactRoomDatabase.invoke(application).contactDao()
        repo = ContactRepository(dao)
        list = repo.contact
    }
}