package com.akrwt.whatsappclone

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    lateinit var currentUserId: String
    lateinit var mAuth: FirebaseAuth
    lateinit var rootRef: DatabaseReference
    private var REQ_CODE = 10
    lateinit var userProfileImageRef: StorageReference
    var builder: AlertDialog.Builder? = null
    var dialog: AlertDialog? = null
    lateinit var toolbar:Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        toolbar=findViewById(R.id.setting_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitle("Account Settings")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowCustomEnabled(true)


        builder = AlertDialog.Builder(this)
        builder!!.setView(R.layout.progressbarimageupload)
        builder!!.setCancelable(false)
        dialog = builder!!.create()

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images")


        setUserName.visibility = View.INVISIBLE

        updateSettingsBtn.setOnClickListener {
            updateSettings()
        }

        retrieveUserInfo()

        set_profile_image.setOnClickListener {
            var galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.setType("image/*")
            startActivityForResult(galleryIntent, REQ_CODE)
        }
    }

    private fun updateSettings() {
        val userName = setUserName.text.toString()
        val status = setUserStatus.text.toString()

        if (TextUtils.isEmpty(userName))
            Toast.makeText(applicationContext, "Please provide user name", Toast.LENGTH_LONG).show()
        if (TextUtils.isEmpty(status))
            Toast.makeText(
                applicationContext,
                "Please provide user status",
                Toast.LENGTH_LONG
            ).show()
        else {
            val profileMap: HashMap<String, Any> = HashMap()
            profileMap["uid"] = currentUserId
            profileMap["name"] = userName
            profileMap["status"] = status
            rootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sendUserToMainActivity()
                        Toast.makeText(
                            applicationContext,
                            "Profile Updated Successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    } else
                        Toast.makeText(
                            applicationContext,
                            "Error : ${task.exception.toString()}",
                            Toast.LENGTH_LONG
                        ).show()
                }
        }
    }

    private fun retrieveUserInfo() {

        rootRef.child("Users").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    if (dataSnapShot.exists() && dataSnapShot.hasChild("name") && dataSnapShot.hasChild(
                            "image"
                        )
                    ) {

                        val retrieveName = dataSnapShot.child("name").getValue().toString()
                        val retrieveStatus = dataSnapShot.child("status").getValue().toString()
                        val retrieveProfilePicture = dataSnapShot.child("image").value.toString()

                        setUserName.setText(retrieveName)
                        setUserStatus.setText(retrieveStatus)
                        var profileImage=findViewById<ImageView>(R.id.set_profile_image)
                        Picasso.get()
                            .load(retrieveProfilePicture)
                            .fit()
                            .into(profileImage)

                    } else if (dataSnapShot.exists() && dataSnapShot.hasChild("name")) {
                        val retrieveName = dataSnapShot.child("name").getValue().toString()
                        val retrieveStatus = dataSnapShot.child("status").getValue().toString()

                        setUserName.setText(retrieveName)
                        setUserStatus.setText(retrieveStatus)

                    } else {
                        setUserName.visibility = View.VISIBLE
                        Toast.makeText(
                            applicationContext,
                            "Please set and update your profile information",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            })

    }


    private fun sendUserToMainActivity() {
        val i = Intent(applicationContext, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
            var imageUri = data.data
            CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {

                dialog!!.show()

                val resultUri = result.uri

                val filePath =
                    userProfileImageRef.child(currentUserId)

                filePath.putFile(resultUri)
                    .addOnSuccessListener {
                        filePath.downloadUrl.addOnSuccessListener {

                            rootRef.child("Users").child(currentUserId).child("image")
                                .setValue(it.toString())
                                .addOnCompleteListener { task ->
                                    dialog!!.dismiss()
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Image Saved in the database",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            task.exception.toString(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        dialog!!.dismiss()
                                    }
                                }

                            Toast.makeText(
                                applicationContext,
                                "Profile Image Uploaded Successfully",
                                Toast.LENGTH_LONG
                            ).show()

                        }

                            .addOnFailureListener {
                                Toast.makeText(
                                    applicationContext,
                                    it.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                dialog!!.dismiss()

                            }
                    }
            }
        }
    }
}
