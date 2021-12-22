package com.whatsapp.clone

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.text.TextUtils

import io.agora.rtc.RtcEngine

import android.graphics.PorterDuff

import android.widget.Toast

import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.NonNull

import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import io.agora.rtc.Constants

import io.agora.rtc.IRtcEngineEventHandler
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*
import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import com.whatsapp.clone.databaseprocess.firebase_CRUD
import com.whatsapp.clone.databinding.ActivityVoiceCallBinding
import com.whatsapp.clone.model.CallModel
import com.whatsapp.clone.model.MyProfileModel
import kotlin.collections.HashMap
import kotlin.math.roundToInt


class VoiceCallActivity : AppCompatActivity() {
    private val LOG_TAG: String = VoiceCallActivity::class.java.getSimpleName()

    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22

    private var mRtcEngine // Tutorial Step 1
            : RtcEngine? = null
    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Tutorial Step 1
        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         * Leave the channel: When the user/host leaves the channel, the user/host sends a goodbye message. When this message is received, the SDK determines that the user/host leaves the channel.
         * Drop offline: When no data packet of the user or host is received for a certain period of time (20 seconds for the communication profile, and more for the live broadcast profile), the SDK assumes that the user/host drops offline. A poor network connection may lead to false detections, so we recommend using the Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who
         * leaves
         * the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         * USER_OFFLINE_QUIT(0): The user left the current channel.
         * USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         * USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        override fun onUserOffline(uid: Int, reason: Int) { // Tutorial Step 4
            runOnUiThread { onRemoteUserLeft(uid, reason) }
        }


        /**
         * Occurs when a remote user stops/resumes sending the audio stream.
         * The SDK triggers this callback when the remote user stops or resumes sending the audio stream by calling the muteLocalAudioStream method.
         *
         * @param uid ID of the remote user.
         * @param muted Whether the remote user's audio stream is muted/unmuted:
         *
         * true: Muted.
         * false: Unmuted.
         */
        override fun onUserMuteAudio(uid: Int, muted: Boolean) { // Tutorial Step 6
            runOnUiThread { onRemoteUserVoiceMuted(uid, muted) }
        }
    }


    private fun callNotification() {

        val crud = firebase_CRUD()
        val map = HashMap<String, String>()
        map.put("id", crud.getCurrentId())
        map.put("call_state", "voice")
        crud.database.collection("call$profile_id").document("call").set(map)


    }


    private lateinit var binding: ActivityVoiceCallBinding
    private var timerStarted = false
    private var time = 0.0
    private lateinit var serviceIntent: Intent
    var profile_id = ""
    var name = ""
    var profile_photo = ""
    var crud = firebase_CRUD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel()
        }

        name = intent.getStringExtra("profile_name").toString()
        profile_photo = intent.getStringExtra("profile_photo").toString()
        profile_id = intent.getStringExtra("profile_id").toString()

        if (!profile_id.equals(""))
            callNotification()

        binding.userName.text = name
        Picasso.get().load(profile_photo).fit().centerCrop().into(binding.userPhoto)

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATE))
        checkCall()

    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            time = intent?.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)!!
            binding.timer.text = getTimeStringFromDouble(time)
        }

    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hours: Int, minutes: Int, seconds: Int): String =
        String.format("%02d:%02d:%02d", hours, minutes, seconds)


    private fun startTimer() {
        binding.timer.visibility = View.VISIBLE
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        timerStarted = true
    }

    private fun stopTimer() {
        stopService(serviceIntent)
        timerStarted = false
    }


    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine() // Tutorial Step 1
        joinChannel() // Tutorial Step 2
    }

    fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(LOG_TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)
        when (requestCode) {
            PERMISSION_REQ_ID_RECORD_AUDIO -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    initAgoraEngineAndJoinChannel()
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                    finish()
                }
            }
        }
    }

    fun showLongToast(msg: String?) {
        runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
        stopTimer()
        saveCall()
        deleteCall()
    }

    // Tutorial Step 7
    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
        } else {
            iv.isSelected = true
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    // Tutorial Step 5
    fun onSwitchSpeakerphoneClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
        } else {
            iv.isSelected = true
        }

        // Enables/Disables the audio playback route to the speakerphone.
        //
        // This method sets whether the audio is routed to the speakerphone or earpiece. After calling this method, the SDK returns the onAudioRouteChanged callback to indicate the changes.
        mRtcEngine!!.setEnableSpeakerphone(view.isSelected())
    }

    // Tutorial Step 3
    fun onEncCallClicked(view: View?) {
        finish()
    }

    // Tutorial Step 1
    private fun initializeAgoraEngine() {
        mRtcEngine = try {
            RtcEngine.create(
                baseContext,
                getString(com.whatsapp.clone.R.string.agora_app_id),
                mRtcEventHandler
            )
        } catch (e: Exception) {
            Log.e(LOG_TAG, Log.getStackTraceString(e))
            throw RuntimeException(
                """
                     NEED TO check rtc sdk init fatal error
                     ${Log.getStackTraceString(e)}
                     """.trimIndent()
            )
        }
    }

    private fun checkCall() {
        if (profile_id.equals(""))
            crud.database.collection("call${crud.getCurrentId()}").document("call")
                .addSnapshotListener { value, error ->

                    if (value?.get("id").toString() == "null") {
                        finish()

                    } else
                        stopTimer().also { startTimer() }

                } else
            crud.database.collection("call${profile_id}").document("call")
                .addSnapshotListener { value, error ->

                    if (value?.get("id").toString() == "null") {
                        finish()
                    } else
                        stopTimer().also { startTimer() }
                }

    }

    // Tutorial Step 2
    private fun joinChannel() {
        var accessToken: String? = getString(com.whatsapp.clone.R.string.agora_token)
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(
                accessToken,
                "#YOUR ACCESS TOKEN#"
            )
        ) {
            accessToken = null // default, no token
        }

        // Sets the channel profile of the Agora RtcEngine.
        // CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile. Use this profile in one-on-one calls or group calls, where all users can talk freely.
        // CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams; an audience can only receive streams.
        mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)

        // Allows a user to join a channel.
        mRtcEngine!!.joinChannel(
            accessToken,
            getString(com.whatsapp.clone.R.string.agora_channel_name),
            "Extra Optional Data",
            0
        ) // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 3
    private fun leaveChannel() {
        mRtcEngine!!.leaveChannel()
    }

    // Tutorial Step 4
    private fun onRemoteUserLeft(uid: Int, reason: Int) {
        showLongToast(
            java.lang.String.format(
                Locale.US,
                "user %d left %d",
                uid and 0xFFFFFFFFL.toInt(),
                reason
            )
        )
        finish()
    }


    private fun deleteCall() {
        if (!profile_id.equals(""))
            crud.database.collection("call$profile_id").document("call").delete()

    }


    private fun saveCall() {

        crud.database.collection("mycalls${crud.getCurrentId()}").add(
            CallModel(
                "voice",
                MyProfileModel(
                    name,
                    profile_photo
                ),
                if (profile_id.equals("")) 2 else 1,
                binding.timer.text.toString(),
                Timestamp.now()
            )
        )


        deleteCall()
    }

    // Tutorial Step 6
    private fun onRemoteUserVoiceMuted(uid: Int, muted: Boolean) {
        showLongToast(
            java.lang.String.format(
                Locale.US, "user %d muted or unmuted %b",
                uid and 0xFFFFFFFFL.toInt(), muted
            )
        )
    }
}