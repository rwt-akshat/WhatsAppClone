package com.akrwt.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().getReference()

        already_have_an_account_link.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerButton.setOnClickListener {
            createNewAccount()
        }

    }

    private fun createNewAccount() {
        val email = register_email.text.toString()
        val password = register_password.text.toString()

        if (TextUtils.isEmpty(email)) {
            register_email.error = "Please enter email"
            register_email.requestFocus()
        }
        if (TextUtils.isEmpty(password)) {
            register_password.error = "Please enter password"
            register_password.requestFocus()
        } else {

            var builder = AlertDialog.Builder(this)
            builder.setView(R.layout.progress)
            val dialog = builder.create()
            dialog.show()

            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        var deviceToken= FirebaseInstanceId.getInstance().getToken()

                        val currentUserId = mAuth!!.currentUser!!.uid
                        rootRef!!.child("Users").child(currentUserId).setValue("")

                        rootRef!!.child("Users").child(currentUserId).child("device_token")
                            .setValue(deviceToken)


                        val i = Intent(this, MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(i)
                        finish()

                        Toast.makeText(
                            applicationContext,
                            "Account Created Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Error: ${task.exception.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }
        }
    }
}
