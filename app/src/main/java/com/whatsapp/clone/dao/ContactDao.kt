package com.whatsapp.clone.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.whatsapp.clone.model.MyProfileModel
import com.whatsapp.clone.model.ProfileDatabase


@Dao
interface ContactDao {


    @Query("SELECT * FROM profile_table")
    fun getContactList(): LiveData<List<ProfileDatabase>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addContact(vararg data: ProfileDatabase)

    @Query("DELETE FROM profile_table")
    fun deleteAllContact()


}