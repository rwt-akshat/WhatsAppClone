package com.akrwt.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.progress.*

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var userRef:DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userRef=FirebaseDatabase.getInstance().reference.child("Users")

        mAuth = FirebaseAuth.getInstance()

        need_new_account_link.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            allowUserToLogin()
        }
        phone_login_button.setOnClickListener {
            startActivity(Intent(this, PhoneLoginActivity::class.java))
        }

    }

    private fun allowUserToLogin() {
        val email = login_email.text.toString()
        val password = login_password.text.toString()

        if (TextUtils.isEmpty(email)) {
            login_email.error = "Please enter email"
            login_email.requestFocus()
        }
        if (TextUtils.isEmpty(password)) {
            login_password.error = "Please enter password"
            login_password.requestFocus()
        } else {

            var builder = AlertDialog.Builder(this)
            builder.setView(R.layout.progresssignin)
            val dialog = builder.create()
            dialog.show()

            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        var currentUserId=mAuth!!.currentUser!!.uid
                        var deviceToken= FirebaseInstanceId.getInstance().getToken()

                        userRef!!.child(currentUserId).child("device_token")
                            .setValue(deviceToken)
                            .addOnCompleteListener{
                                if(it.isSuccessful){
                                    val i = Intent(this, MainActivity::class.java)
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(i)
                                    finish()
                                    Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT)
                                        .show()
                                    dialog.dismiss()

                                }
                            }


                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Login UnSuccessful,Error:${task.exception.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }

                }
        }
    }
}
