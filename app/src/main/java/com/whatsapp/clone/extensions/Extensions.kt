package com.whatsapp.clone.extensions

import agency.tango.android.avatarview.views.AvatarView
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.whatsapp.clone.R
import com.whatsapp.clone.ui.login.VerifiedPhoneFragmentDirections
import java.lang.NumberFormatException
import javax.crypto.Cipher.SECRET_KEY
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.R.string
import android.content.Intent
import android.graphics.drawable.Drawable
import com.google.firebase.Timestamp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.*
import javax.crypto.spec.PBEKeySpec
import android.view.animation.Animation

import android.view.animation.AnimationSet

import android.view.animation.DecelerateInterpolator

import android.view.animation.ScaleAnimation

import android.view.animation.RotateAnimation

import androidx.core.content.ContextCompat

import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import com.whatsapp.clone.VideoCallActivity
import com.whatsapp.clone.VoiceCallActivity
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.model.MyProfileModel


fun Context.toastShow(message: String) {

    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun Activity.dialogEditShow(phone: String, navController: NavController) {

    val view = LayoutInflater.from(this).inflate(R.layout.custom_verified_dialog, null)

    val builder = AlertDialog.Builder(this).setView(view).show()
    builder.setCancelable(false)

    builder.findViewById<TextView>(R.id.custom_edit_button).setOnClickListener {
        builder.dismiss()
    }

    builder.findViewById<TextView>(R.id.custom_ok_button).setOnClickListener {
        builder.dismiss()
        this.hideKeyboard()
        navController.navigate(
            VerifiedPhoneFragmentDirections.actionVerifiedPhoneFragmentToSmsVerifiedFragment(phone)
        )
    }

    builder.findViewById<TextView>(R.id.custom_phone_number).text = phone


}

fun Activity.dialogShow(message: String): AlertDialog {

    val view = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)

    val builder = AlertDialog.Builder(this).setView(view).show()

    builder.findViewById<TextView>(R.id.custom_message).text = message

    builder.setCancelable(false)

    return builder
}


fun Activity.callShow(profile: MyProfileModel, state: String): AlertDialog {

    val view = LayoutInflater.from(this).inflate(R.layout.call_design, null)

    val builder = AlertDialog.Builder(this).setView(view).show()

    builder.findViewById<TextView>(R.id.call_name).text = profile.name
    builder.findViewById<TextView>(R.id.call_phone).text = profile.phone_number
    profile.profile_photo?.let { builder.findViewById<ImageView>(R.id.callPhoto).upload(it) }

    builder.findViewById<TextView>(R.id.reject).setOnClickListener {
        val crud = firebase_CRUD()
        crud.database.collection("call${crud.getCurrentId()}").document("call")
            .delete()
        builder.dismiss()
    }


    builder.findViewById<TextView>(R.id.accept).setOnClickListener {
        val intent = if (state.equals("video"))
            Intent(applicationContext, VideoCallActivity::class.java)
        else
            Intent(applicationContext, VoiceCallActivity::class.java)
        intent.putExtra("profile_name", profile.name)
        intent.putExtra("profile_photo", profile.profile_photo)
        intent.putExtra("profile_id", "")
        startActivity(intent)
        builder.dismiss()
    }

    builder.setCancelable(false)

    return builder
}


fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}


fun formatNumberToE164(phNum: String, countryCode: String): String {

    var e164Number: String?

    if (TextUtils.isEmpty(countryCode)) {
        e164Number = phNum
    } else {
        try {
            e164Number = PhoneNumberUtils.formatNumberToE164(phNum, countryCode)
        } catch (e: NumberFormatException) {
            Log.e(ContentValues.TAG, "Caught: " + e.message.toString(), e)
            e164Number = phNum
        }
    }
    return e164Number!!
}

const val secretKey = "662ede816988e58fb6d057d9d85605e0"

fun String.encrypt(): String? {
    Security.addProvider(BouncyCastleProvider())
    var keyBytes: ByteArray

    try {
        keyBytes = secretKey.toByteArray(charset("UTF8"))
        val skey = SecretKeySpec(keyBytes, "AES")
        val input = this.toByteArray(charset("UTF8"))

        synchronized(Cipher::class.java) {
            val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, skey)

            val cipherText = ByteArray(cipher.getOutputSize(input.size))
            var ctLength = cipher.update(
                input, 0, input.size,
                cipherText, 0
            )
            ctLength += cipher.doFinal(cipherText, ctLength)
            return String(
                Base64.encode(cipherText, ctLength)
            )
        }
    } catch (uee: UnsupportedEncodingException) {
        uee.printStackTrace()
    } catch (ibse: IllegalBlockSizeException) {
        ibse.printStackTrace()
    } catch (bpe: BadPaddingException) {
        bpe.printStackTrace()
    } catch (ike: InvalidKeyException) {
        ike.printStackTrace()
    } catch (nspe: NoSuchPaddingException) {
        nspe.printStackTrace()
    } catch (nsae: NoSuchAlgorithmException) {
        nsae.printStackTrace()
    } catch (e: ShortBufferException) {
        e.printStackTrace()
    }

    return null
}

fun String.decryptWithAES(): String? {
    Security.addProvider(BouncyCastleProvider())
    var keyBytes: ByteArray

    try {
        keyBytes = secretKey.toByteArray(charset("UTF8"))
        val skey = SecretKeySpec(keyBytes, "AES")
        val input = org.bouncycastle.util.encoders.Base64
            .decode(this?.trim { it <= ' ' }?.toByteArray(charset("UTF8")))

        synchronized(Cipher::class.java) {
            val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, skey)

            val plainText = ByteArray(cipher.getOutputSize(input.size))
            var ptLength = cipher.update(input, 0, input.size, plainText, 0)
            ptLength += cipher.doFinal(plainText, ptLength)
            val decryptedString = String(plainText)
            return decryptedString.trim { it <= ' ' }
        }
    } catch (uee: UnsupportedEncodingException) {
        uee.printStackTrace()
    } catch (ibse: IllegalBlockSizeException) {
        ibse.printStackTrace()
    } catch (bpe: BadPaddingException) {
        bpe.printStackTrace()
    } catch (ike: InvalidKeyException) {
        ike.printStackTrace()
    } catch (nspe: NoSuchPaddingException) {
        nspe.printStackTrace()
    } catch (nsae: NoSuchAlgorithmException) {
        nsae.printStackTrace()
    } catch (e: ShortBufferException) {
        e.printStackTrace()
    }

    return null
}


fun getCurrentDay(): Int {

    return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

}

fun getServerTime(): Timestamp {
    return Timestamp.now()

}


fun calcDay(timestamp: Long): String {

    val c = Calendar.getInstance()
    c.timeInMillis = timestamp

    val today = c.get(Calendar.DAY_OF_MONTH)
    val format_t = SimpleDateFormat("dd EE yyyy")
    val format_tt = SimpleDateFormat("HH:mm")

    return when (today) {

        getCurrentDay() -> "son görülme ${format_tt.format(c.time)}"
        (getCurrentDay() - 1) -> "son görülme dün ${format_tt.format(c.time)}"
        else -> {
            "son görülme ${format_t.format(c.time)}"
        }
    }

}

fun calcLastMessageDiff(timestamp: Long): String {

    val c = Calendar.getInstance()
    c.timeInMillis = timestamp

    val today = c.get(Calendar.DAY_OF_MONTH)
    val format_t = SimpleDateFormat("dd.MM.yyyy")
    val format_tt = SimpleDateFormat("HH:mm")

    return when (today) {

        getCurrentDay() -> "${format_tt.format(c.time)}"
        (getCurrentDay() - 1) -> "Dün"
        else -> {
            "${format_t.format(c.time)}"
        }
    }

}

fun calcStoryTime(timestamp: Long): String {

    val c = Calendar.getInstance()
    c.timeInMillis = timestamp

    val today = c.get(Calendar.DAY_OF_MONTH)
    val format_t = SimpleDateFormat("dd.MM.yyyy")
    val format_tt = SimpleDateFormat("HH:mm")

    return when (today) {

        getCurrentDay() -> "Bugün ${format_tt.format(c.time)}"
        (getCurrentDay() - 1) -> "Dün ${format_tt.format(c.time)}"
        else -> {
            "${format_t.format(c.time)}"
        }
    }

}

fun calcRegDate(timestamp: Long): String {

    val c = Calendar.getInstance()
    c.timeInMillis = timestamp

    val format_t = SimpleDateFormat("dd MMM yyyy")

    return "${format_t.format(c.time)}"


}

val iconList = arrayListOf<Int>(
    R.drawable.message_icon,
    R.drawable.message_icon,
    R.drawable.camera_icon,
    R.drawable.call_icon,
)


fun FloatingActionButton.animateFab(position: Int) {
    this.clearAnimation()

    val fab = this

    // Scale down animation
    val shrink = ScaleAnimation(
        1f,
        0.1f,
        1f,
        0.1f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )
    shrink.duration = 100 // animation duration in milliseconds
    shrink.interpolator = AccelerateInterpolator()
    shrink.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}
        override fun onAnimationEnd(animation: Animation) {
            // Change FAB color and icon

            fab.setImageResource(
                iconList[position]
            )

            // Rotate Animation
            val rotate: Animation = RotateAnimation(
                60.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 150
            rotate.interpolator = DecelerateInterpolator()

            // Scale up animation
            val expand = ScaleAnimation(
                0.1f,
                1f,
                0.1f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            expand.duration = 150 // animation duration in milliseconds
            expand.interpolator = DecelerateInterpolator()

            // Add both animations to animation state
            val s = AnimationSet(false) //false means don't share interpolators
            s.addAnimation(rotate)
            s.addAnimation(expand)
            fab.startAnimation(s)
        }

        override fun onAnimationRepeat(animation: Animation) {}
    })
    this.startAnimation(shrink)
}

fun ImageView.upload(url: String) {

    Picasso.get()
        .load(url).fit().centerCrop().into(this)
}

fun PhotoView.upload(url: String) {

    Picasso.get()
        .load(url).fit().centerCrop().into(this)
}

fun AvatarView.upload(url: String) {

    Picasso.get()
        .load(url).fit().centerCrop().into(this)
}

@BindingAdapter("uploadAvatar")
fun uploadAvatar(view: AvatarView, url: String) {
    Picasso.get()
        .load(url).fit().centerCrop().into(view)
}

