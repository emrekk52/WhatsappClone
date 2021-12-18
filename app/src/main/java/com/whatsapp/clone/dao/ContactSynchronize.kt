package com.whatsapp.clone.dao

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.whatsapp.clone.extensions.formatNumberToE164
import com.whatsapp.clone.model.ProfileDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class ContactSynchronize(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), CoroutineScope {

    var list = arrayListOf<ProfileDatabase>()

    val dao: ContactDao
    val repo: ContactRepository

    init {
        dao = ContactRoomDatabase.invoke(context).contactDao()
        repo = ContactRepository(dao)
    }

    override suspend fun doWork(): Result {

        getContacts()

        return Result.success()
    }


    @SuppressLint("Range")
    private fun getContacts() {

        val phones = applicationContext.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )


        phones?.let {
            var temp_number = ""
            while (it.moveToNext()) {
                var name =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var number =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                number = formatNumberToE164(number, Locale.getDefault().country)

                if (name.isNullOrEmpty())
                    name = number

                if (temp_number != number) {
                    temp_number = number
                    findUser(number, name)
                }


            }
            it.close()
            addDatabase()

        }

    }


    private fun findUser(number: String, name: String) {

        val query = Firebase.firestore.collection("users")
            .whereEqualTo("phone_number", number)
        var user_model: ProfileDatabase


        query.addSnapshotListener { snapshot, exception ->
            if (exception == null) {
                snapshot?.documents?.forEach {
                    user_model = ProfileDatabase(
                        profile_photo = it["profile_photo"].toString(),
                        name = name,
                        profile_id = it["profile_id"].toString(),
                        phone_number = number,
                        token = it["token"].toString(),
                    )
                    updateList(user_model)
                }
            }
        }

    }

    private fun addDatabase() {

        launch {
            repo.deleteAllContact()
            repo.addContact(list)
        }

    }


    fun updateList(user_model: ProfileDatabase) {
        if (list.size == 0 || !list.get(list.size - 1).profile_id!!.equals(user_model.profile_id)) {
            list.add(user_model)
        }

    }

}