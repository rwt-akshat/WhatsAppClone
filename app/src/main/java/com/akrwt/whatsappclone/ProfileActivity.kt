package com.akrwt.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    var receiverUserId: String? = null
    lateinit var userRef: DatabaseReference
    lateinit var ChatReqRef: DatabaseReference
    lateinit var ContactsRef: DatabaseReference
    lateinit var NotificationRef: DatabaseReference

    lateinit var current_state: String
    lateinit var sender_user_id: String
    lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        mAuth = FirebaseAuth.getInstance()
        sender_user_id = mAuth.currentUser!!.uid

        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        ChatReqRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        ContactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        NotificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")

        receiverUserId = intent.extras!!.get("visit_user_id").toString()

        current_state = "new"

        retrieveUserInfo()


    }

    private fun retrieveUserInfo() {
        userRef.child(receiverUserId!!).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    val userImage = dataSnapshot.child("image").value.toString()
                    val userName = dataSnapshot.child("name").value.toString()
                    val userStatus = dataSnapshot.child("status").value.toString()

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image)
                        .into(visit_profile_image)
                    visit_user_id.setText(userName)
                    visit_user_status.setText(userStatus)


                    manageChatRequest()

                } else {
                    val userName = dataSnapshot.child("name").value.toString()
                    val userStatus = dataSnapshot.child("status").value.toString()

                    visit_user_id.setText(userName)
                    visit_user_status.setText(userStatus)

                    manageChatRequest()
                }
            }


        })


    }

    private fun manageChatRequest() {

        ChatReqRef.child(sender_user_id)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild(receiverUserId!!)) {
                        var request_type =
                            p0.child(receiverUserId!!).child("request_type").getValue().toString()
                        if (request_type.equals("sent")) {
                            current_state = "request_sent"
                            send_msg_btn.text = "Cancel Chat Request"
                        } else if (request_type.equals("received")) {
                            current_state = "request_received"
                            send_msg_btn.setText("Accept Chat Request")

                            decline_msg_req_btn.visibility = View.VISIBLE
                            decline_msg_req_btn.isEnabled = true
                            decline_msg_req_btn.setOnClickListener {
                                CancelChatReq()
                            }
                        }
                    } else {
                        ContactsRef.child(sender_user_id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.hasChild(receiverUserId!!)) {
                                        current_state = "friends"
                                        send_msg_btn.setText("Remove this contact")
                                    }
                                }

                            })
                    }

                }

            })

        if (!sender_user_id.equals(receiverUserId)) {
            send_msg_btn.setOnClickListener {
                send_msg_btn.isEnabled = false
                if (current_state.equals("new"))
                    SendChatReq()
                if (current_state.equals("request_sent"))
                    CancelChatReq()
                if (current_state.equals("request_received"))
                    AcceptChatReq()
                if (current_state.equals("friends"))
                    RemoveSpecificContact()
            }
        } else {
            send_msg_btn.visibility = View.INVISIBLE
        }

    }

    private fun RemoveSpecificContact() {
        ContactsRef.child(sender_user_id).child(receiverUserId!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ContactsRef.child(receiverUserId!!).child(sender_user_id)
                        .removeValue()
                        .addOnCompleteListener { t ->
                            if (t.isSuccessful) {
                                send_msg_btn.isEnabled = true
                                current_state = "new"
                                send_msg_btn.setText("Send Message")

                                decline_msg_req_btn.visibility = View.INVISIBLE
                                decline_msg_req_btn.isEnabled = false
                            }
                        }
                }
            }

    }

    private fun AcceptChatReq() {

        ContactsRef.child(sender_user_id).child(receiverUserId!!)
            .child("Contacts").setValue("Saved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ContactsRef.child(receiverUserId!!).child(sender_user_id)
                        .child("Contacts").setValue("Saved")
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                ChatReqRef.child(sender_user_id).child(receiverUserId!!)
                                    .removeValue()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            ChatReqRef.child(receiverUserId!!)
                                                .child(sender_user_id)
                                                .removeValue()
                                                .addOnCompleteListener {
                                                    send_msg_btn.isEnabled = true
                                                    current_state = "friends"
                                                    send_msg_btn.setText("Remove this contact")

                                                    decline_msg_req_btn.visibility = View.INVISIBLE
                                                    decline_msg_req_btn.isEnabled = false
                                                }

                                        }
                                    }
                            }
                        }

                }
            }

    }

    private fun CancelChatReq() {
        ChatReqRef.child(sender_user_id).child(receiverUserId!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ChatReqRef.child(receiverUserId!!).child(sender_user_id)
                        .removeValue()
                        .addOnCompleteListener { t ->
                            if (t.isSuccessful) {
                                send_msg_btn.isEnabled = true
                                current_state = "new"
                                send_msg_btn.setText("Send Message")

                                decline_msg_req_btn.visibility = View.INVISIBLE
                                decline_msg_req_btn.isEnabled = false
                            }
                        }
                }
            }
    }

    private fun SendChatReq() {
        ChatReqRef.child(sender_user_id).child(receiverUserId!!)
            .child("request_type").setValue("sent")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ChatReqRef.child(receiverUserId!!).child(sender_user_id)
                        .child("request_type").setValue("received")
                        .addOnCompleteListener {

                            if(it.isSuccessful){

                                var chatNotificationMap=HashMap<String,String>()
                                chatNotificationMap.put("from",sender_user_id)
                                chatNotificationMap.put("type","request")

                                NotificationRef.child(receiverUserId!!).push()
                                    .setValue(chatNotificationMap)
                                    .addOnCompleteListener{
                                        if(it.isSuccessful){
                                            send_msg_btn.isEnabled = true
                                            current_state = "request_sent"
                                            send_msg_btn.setText("Cancel Chat Request")
                                        }
                                    }
                            }
                        }
                }
            }

    }
}
