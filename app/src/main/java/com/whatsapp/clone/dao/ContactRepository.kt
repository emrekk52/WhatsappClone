package com.whatsapp.clone.dao

import androidx.lifecycle.LiveData
import com.whatsapp.clone.model.ProfileDatabase

class ContactRepository(private val dao: ContactDao) {

    val contact: LiveData<List<ProfileDatabase>> = dao.getContactList()

     fun addContact(list: List<ProfileDatabase>) {
        dao.addContact(*list.toTypedArray())
    }

    fun deleteAllContact() {
        dao.deleteAllContact()
    }


}