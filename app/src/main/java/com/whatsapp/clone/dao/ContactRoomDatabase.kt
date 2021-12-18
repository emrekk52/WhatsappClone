package com.whatsapp.clone.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.whatsapp.clone.model.ProfileDatabase


@Database(entities = arrayOf(ProfileDatabase::class), version = 1, exportSchema = false)
abstract class ContactRoomDatabase : RoomDatabase() {


    abstract fun contactDao(): ContactDao


    companion object {


        @Volatile
        private var instance: ContactRoomDatabase? = null

        operator fun invoke(context: Context) = instance ?: synchronized(this) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }


        fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ContactRoomDatabase::class.java,
            "profile_table"
        ).build()


    }


}