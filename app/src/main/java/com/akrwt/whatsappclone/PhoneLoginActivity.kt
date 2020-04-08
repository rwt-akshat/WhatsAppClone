package com.akrwt.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_phone_login.*
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var mAuth: FirebaseAuth
    private var builder:AlertDialog.Builder?=null
    private var dialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        builder=AlertDialog.Builder(this)
        builder!!.setView(R.layout.phoneauthprogressbar)
        builder!!.setCancelable(false)
        dialog = builder!!.create()

        mAuth = FirebaseAuth.getInstance()

        send_verification_code_btn.setOnClickListener {

            val phone_number = phone_number_input.text.toString()
            if (TextUtils.isEmpty(phone_number))
                phone_number_input.error = "Please enter your phone number first"
            else {

                dialog!!.show()

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+91$phone_number",
                    60,
                    TimeUnit.SECONDS,
                    this,
                    callbacks
                )
            }

        }



        verify_btn.setOnClickListener {
            send_verification_code_btn.visibility = View.VISIBLE
            phone_number_input.visibility = View.VISIBLE

            val verificationCode = verification_code_input.text.toString()
            if (TextUtils.isEmpty(verificationCode))
                verification_code_input.error = "This field is empty"
            else {

                dialog!!.show()

                val credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode)
                signInWithPhoneAuthCredential(credential)

            }

        }

        verification()

    }

    private fun verification() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                dialog!!.dismiss()
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()

                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()

                }

                send_verification_code_btn.visibility = View.VISIBLE
                phone_number_input.visibility = View.VISIBLE

                verify_btn.visibility = View.INVISIBLE
                verification_code_input.visibility = View.INVISIBLE

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                dialog!!.dismiss()

                mVerificationId = verificationId
                resendToken = token
                Toast.makeText(applicationContext, "Code has been sent", Toast.LENGTH_LONG).show()

                send_verification_code_btn.visibility = View.INVISIBLE
                phone_number_input.visibility = View.INVISIBLE

                verify_btn.visibility = View.VISIBLE
                verification_code_input.visibility = View.VISIBLE

            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    dialog!!.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            applicationContext,
                            task.exception.toString(),
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
            }
    }
}
