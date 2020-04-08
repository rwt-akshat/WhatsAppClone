package com.akrwt.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_group_chat.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class GroupChatActivity : AppCompatActivity() {

    lateinit var currentGrpName: String
    lateinit var currentUserId: String
    lateinit var currentUserName: String
    lateinit var mAuth: FirebaseAuth
    lateinit var UsersRef: DatabaseReference
    lateinit var GroupNameRef: DatabaseReference
    lateinit var groupMsgKeyRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        currentGrpName = intent.extras!!.get("groupName").toString()

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        GroupNameRef =
            FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGrpName)

        val mToolbar = findViewById<Toolbar>(R.id.group_chat_bar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = currentGrpName

        getUserInfo()

        send_message_btn.setOnClickListener {
            saveMessageInfoToDatabase()
            input_group_message.setText("")
            myScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }

    }

    private fun getUserInfo() {

        UsersRef.child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    currentUserName = p0.child("name").value.toString()
                }
            }
        })
    }

    private fun saveMessageInfoToDatabase() {
        val message = input_group_message.text.toString()
        val messageKey = GroupNameRef.push().key
        if (TextUtils.isEmpty(message))
            input_group_message.error = "Please write a message first"
        else {
            val ccalForDate = Calendar.getInstance()
            val currentDate = SimpleDateFormat("MMM dd, yyyy").format(ccalForDate.time)

            val ccalForTime = Calendar.getInstance()
            val currentTime = SimpleDateFormat("hh:mm a").format(ccalForTime.time)

            val grpMessageKey = HashMap<String, Any>()
            GroupNameRef.updateChildren(grpMessageKey)

            groupMsgKeyRef = GroupNameRef.child(messageKey!!)

            val msgInfoMap = HashMap<String, Any>()
            msgInfoMap.put("name", currentUserName)
            msgInfoMap.put("message", message)
            msgInfoMap.put("date", currentDate)
            msgInfoMap.put("time", currentTime)

            groupMsgKeyRef.updateChildren(msgInfoMap)

        }
    }

    override fun onStart() {
        super.onStart()
        GroupNameRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                if (p0.exists())
                    displayMessage(p0)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if (p0.exists())
                    displayMessage(p0)

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

    private fun displayMessage(dataSnapshot: DataSnapshot) {

        var iterator = dataSnapshot.children.iterator()
        while (iterator.hasNext()) {
            var chatDate = iterator.next().value.toString()
            var chatMessage = iterator.next().value.toString()
            var chatName = iterator.next().value.toString()
            var chatTime = iterator.next().value.toString()


            group_chat_text_display.append("$chatName :\n$chatMessage\n$chatTime       $chatDate\n\n\n")

            myScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }

    }

}
