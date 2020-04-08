package com.akrwt.whatsappclone

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout
    lateinit var myTabsAccessorAdapter: TabsAccessorAdapter
    lateinit var mAuth: FirebaseAuth
    private var rootRef: DatabaseReference? = null
    private var currentUserId:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference

        mToolbar = findViewById(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "WhatsApp"




        viewPager = findViewById(R.id.main_tabs_pager)
        myTabsAccessorAdapter = TabsAccessorAdapter(getSupportFragmentManager())
        viewPager.adapter = myTabsAccessorAdapter

        tabLayout = findViewById(R.id.main_tabs)
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onStart() {
        super.onStart()

        var currentUser = mAuth.currentUser

        if (currentUser == null) {
            sendUserToLoginActivity()
        } else {

            updateUserStatus("online")
            VerifyUserExistance()
        }
    }

    override fun onStop() {
        super.onStop()
        var currentUser = mAuth.currentUser

        if(currentUser !=null){
            updateUserStatus("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        var currentUser = mAuth.currentUser
        if(currentUser !=null){
            updateUserStatus("offline")
        }

    }

    private fun VerifyUserExistance() {
        var currentUserId = mAuth.currentUser!!.uid
        rootRef!!.child("Users").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    if (dataSnapShot.child("name").exists())
                        Toast.makeText(applicationContext, "Welcome", Toast.LENGTH_SHORT).show()
                    else {
                        val i = Intent(applicationContext, SettingsActivity::class.java)
                        startActivity(i)
                    }
                }
            })

    }

    private fun sendUserToLoginActivity() {
        val i = Intent(applicationContext, LoginActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)



        when (item.itemId) {
            R.id.main_find_friends_option -> {
                startActivity(Intent(this,FindFriendsActivity::class.java))
            }
            R.id.main_create_group_option -> {
                requestNewGroup()

            }
            R.id.main_settings_option -> {

                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.main_logout_option -> {

                updateUserStatus("offline")
                mAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        return true
    }

    private fun requestNewGroup() {
        var builder = AlertDialog.Builder(this, R.style.alertDialog)
        builder.setTitle("Enter Group Name")

        var grpNameField = EditText(this)
        grpNameField.setHint("My Group")
        builder.setView(grpNameField)

        builder.setPositiveButton("Create") { dialog: DialogInterface, i: Int ->
            var groupName = grpNameField.text.toString()

            if (TextUtils.isEmpty(groupName))
                Toast.makeText(
                    applicationContext,
                    "Please write group name",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                    createNewGroup(groupName)
            }
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int ->

            dialog!!.cancel()
        }

        builder.show()
    }

    private fun createNewGroup(groupName:String) {
        rootRef!!.child("Groups").child(groupName).setValue("")
            .addOnCompleteListener {task->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext,"$groupName group is created successfully",Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun updateUserStatus(state:String){

        var saveCurrentTime:String?=null
        var saveCurrentDate:String?=null

        val calendar=Calendar.getInstance()
        val currentDate=SimpleDateFormat("MMM dd ,yyyy")
        saveCurrentDate=currentDate.format(calendar.time)


        val currentTime=SimpleDateFormat("hh:mm a")
        saveCurrentTime=currentTime.format(calendar.time)


        val onlineState=HashMap<String,Any>()
        onlineState.put("time",saveCurrentTime)
        onlineState.put("date",saveCurrentDate)
        onlineState.put("state",state)



        currentUserId=mAuth.currentUser!!.uid
        rootRef!!.child("Users").child(currentUserId!!).child("userState")
            .updateChildren(onlineState)

    }
}
