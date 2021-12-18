package com.whatsapp.clone.ui.login

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.whatsapp.clone.R
import com.whatsapp.clone.databinding.FragmentSmsVerifiedBinding
import com.whatsapp.clone.extensions.dialogShow
import com.whatsapp.clone.extensions.toastShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.sql.Time
import java.util.concurrent.TimeUnit
import com.whatsapp.clone.MainActivity

import android.content.Intent

import com.google.firebase.auth.AuthResult

import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnCompleteListener


class SmsVerifiedFragment : Fragment() {

    val args: SmsVerifiedFragmentArgs by navArgs()
    lateinit var binding: FragmentSmsVerifiedBinding
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    var storedVerificationId = ""
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    lateinit var dialog: Dialog
    lateinit var dialogVerified: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSmsVerifiedBinding.inflate(layoutInflater)

        dialog = requireActivity().dialogShow("Bağlanıyor..")

        sendSMS()

        binding.wpDg.text =
            Html.fromHtml(
                "<font color=\"#000000\">" + "<b>${args.phone}</b>" + "</font>" + " numarasına gönderilen SMS otomatik olarak tespit edilmeye çalışılıyor." +
                        "<font color=\"#0EA9EF\">" + " Numara yanlış mı?" + "</font>"
            )



        binding.phoneVerified.text = Html.fromHtml("Doğrula <b>${args.phone}</b>")


        binding.resendCode.setOnClickListener {
            resendOTPCode()
        }

        binding.verifiedCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                println(count)
                if (count == 7) {
                    verifyCode(binding.verifiedCode.text.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        return binding.root
    }


    private fun sendSMS() {


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")


                credential.smsCode?.let {
                    verifyCode(it)
                    binding.verifiedCode.setText(it)
                }

                dialog.dismiss()

            }

            override fun onVerificationFailed(e: FirebaseException) {

                Log.w(ContentValues.TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    context?.toastShow("Geçersiz istek")
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    context?.toastShow("Bugün için çok fazla istek yaptınız")
                } else
                    context?.toastShow(e.localizedMessage.toString())

                dialog.dismiss()
                findNavController().navigateUp()

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                Log.d(ContentValues.TAG, "onCodeSent:$verificationId")

                storedVerificationId = verificationId //gönderilen kodu geri alırken doğrulama anahtarı
                resendToken = token // kodun süresinin bitmesinden ya da çeşitli hatalar olması durumunda tekrar kod gönderme tokeni


                dialog.dismiss()
                startTimer()

            }
        }


        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(args.phone) //girilen telefon numarasının alındığı yer
            .setTimeout(60L, TimeUnit.SECONDS) //kodun kaç saniye içerisinde doğrulanması gerektiğini belirtir
            .setActivity(requireActivity())
            .setCallbacks(callbacks) //kod gönderildikten sonra hata, tamamlanma ve doğrulama işlemlerinin yakalandığı fonksiyon kısmı
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)


    }


    private fun startTimer() {
        binding.resendCode.isEnabled = false

        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timer.text =
                    "00:${if (millisUntilFinished > 10) "${millisUntilFinished / 1000}" else "0${millisUntilFinished / 1000}"}"
            }

            override fun onFinish() {
                binding.timer.text = "00:00"
                binding.resendCode.isEnabled = true
            }
        }
        timer.start()

    }


    private fun resendOTPCode() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            args.phone,        // Phone number to verify
            60L,                 // Timeout duration
            TimeUnit.SECONDS,   // Unit of timeout
            requireActivity(),               // Activity (for callback binding)
            callbacks,         // OnVerificationStateChangedCallbacks
            resendToken
        )
        startTimer()

    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(OnCompleteListener<AuthResult?> { task ->
                if (task.isSuccessful) {
                    activity?.toastShow("Onaylandı")
                    findNavController().navigate(
                        SmsVerifiedFragmentDirections.actionNavSmsToNavProfileEdit(args.phone)
                    )
                } else {
                    activity?.toastShow(task.exception?.message.toString())
                }

                dialogVerified.dismiss()

            })
    }

    private fun verifyCode(code: String) {
        code.trim()
        val arr = code.split(" ")
        val newCode = arr[0] + arr[1]
        dialogVerified = requireActivity().dialogShow("Doğrulanıyor..")
        dialogVerified.show()
        val credential = PhoneAuthProvider.getCredential(
            storedVerificationId,
            newCode
        )
        signInWithCredential(credential)
    }


}