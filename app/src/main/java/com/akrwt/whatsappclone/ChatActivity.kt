package com.akrwt.whatsappclone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.*
import android.app.AlertDialog
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.custom_chat_bar.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    lateinit var messageReceiverId: String
    lateinit var messageReceiverName: String
    lateinit var messageReceiverImage: String
    lateinit var mAuth: FirebaseAuth
    lateinit var messageSenderId: String
    lateinit var rootRef: DatabaseReference
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    lateinit var messageList: ArrayList<Messages>
    lateinit var messageAdapter: MessageAdapter
    lateinit var userMessageList: RecyclerView
    private var checker = ""
    private var fileUri: Uri? = null
    private var myURL: String = ""
    private var uploadTask: UploadTask? = null
    lateinit var dialog: AlertDialog

    var saveCurrentTime: String? = null
    var saveCurrentDate: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        dialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Please wait,we are sending that file")
            .setCancelable(false)
            .build()

        mAuth = FirebaseAuth.getInstance()
        messageSenderId = mAuth.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference

        messageReceiverId = intent.extras!!.get("visit_user_id").toString()
        messageReceiverName = intent.extras!!.get("visit_user_name").toString()

        messageReceiverImage = intent.extras!!.get("visit_image").toString()

        toolbar = findViewById(R.id.chat_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null)
        supportActionBar!!.setCustomView(actionBarView)

        messageList = ArrayList()
        userMessageList = findViewById(R.id.private_msg_list_of_users)
        messageAdapter = MessageAdapter(messageList)
        userMessageList.layoutManager = LinearLayoutManager(this)

        userMessageList.adapter = messageAdapter


        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd ,yyyy")
        saveCurrentDate = currentDate.format(calendar.time)

        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)


        custom_profile_name.setText(messageReceiverName)
        Picasso.get()
            .load(messageReceiverImage)
            .placeholder(R.drawable.profile_image)
            .into(custom_profile_image)


        send_message_button.setOnClickListener {
            sendMessage()
        }

        displayLastSeen()

        send_files_button.setOnClickListener {

            val d = AlertDialog.Builder(this)
            val options = arrayOf(
                "Image",
                "PDF",
                "Document"
            )


            d.setItems(options) { _, which ->

                when (which) {
                    0 -> {
                        checker = "image"
                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "image/*"
                        startActivityForResult(Intent.createChooser(intent, "Select Image"), 438)
                    }
                    1 -> {
                        checker = "pdf"
                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "application/pdf"
                        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 438)
                    }
                    2 -> {
                        checker = "docx"
                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "application/msword"
                        startActivityForResult(
                            Intent.createChooser(intent, "Select word file"),
                            438
                        )
                    }
                }
            }
            d.create()
            val dialogAlert = d.show()
            dialogAlert.window!!.setLayout(700, 500)
        }

    }

    private fun sendMessage() {
        val messageText = input_message.text.toString()
        if (TextUtils.isEmpty(messageText)) {
            input_message.error = "This Field is empty"
            input_message.requestFocus()
        } else {
            var messageSenderRef = "Messages/$messageSenderId/$messageReceiverId"
            var messageReceiverRef = "Messages/$messageReceiverId/$messageSenderId"

            var userMsgKeyRef = rootRef.child("Messages")
                .child(messageSenderId)
                .child(messageReceiverId)
                .push()

            var messagePushId = userMsgKeyRef.key
            var messageTextBody = HashMap<String, String>()
            messageTextBody.put("message", messageText)
            messageTextBody.put("type", "text")
            messageTextBody.put("from", messageSenderId)

            messageTextBody.put("to", messageReceiverId)
            messageTextBody.put("messageID", messagePushId!!)
            messageTextBody.put("time", saveCurrentTime!!)
            messageTextBody.put("date", saveCurrentDate!!)


            var messageBodyDetails = HashMap<String, Any>()
            messageBodyDetails.put("$messageSenderRef/$messagePushId", messageTextBody)

            messageBodyDetails.put("$messageReceiverRef/$messagePushId", messageTextBody)

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener {
                if (it.isSuccessful)
                    Toast.makeText(
                        applicationContext,
                        "Message Sent Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                else
                    Toast.makeText(
                        applicationContext,
                        "Error:${it.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                input_message.setText("")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val messages = p0.getValue(Messages::class.java)
                    messageList.add(messages!!)
                    messageAdapter.notifyDataSetChanged()

                    userMessageList.smoothScrollToPosition(userMessageList.adapter!!.itemCount)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }

            })
    }


    fun displayLastSeen() {
        rootRef.child("Users").child(messageReceiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child("userState").hasChild("state")) {
                        val state = p0.child("userState").child("state").getValue().toString()
                        val date = p0.child("userState").child("date").getValue().toString()
                        val time = p0.child("userState").child("time").getValue().toString()

                        if (state.equals("online")) {
                            custom_user_last_seen.setText("online")
                        } else if (state.equals("offline")) {
                            custom_user_last_seen.setText("Last Seen: $date  $time")

                        }

                    } else {
                        custom_user_last_seen.setText("offline")
                    }

                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            dialog.show()

            fileUri = data.data
            if (!checker.equals("image")) {


                val storageReference =
                    FirebaseStorage.getInstance().reference.child("Document Files")

                val messageSenderRef = "Messages/$messageSenderId/$messageReceiverId"
                val messageReceiverRef = "Messages/$messageReceiverId/$messageSenderId"

                val userMsgKeyRef = rootRef.child("Messages")
                    .child(messageSenderId)
                    .child(messageReceiverId)
                    .push()

                val messagePushId = userMsgKeyRef.key
                val filePath = storageReference.child(messagePushId!! + "." + checker)


                filePath.putFile(fileUri!!).addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener {

                        val messageBody = HashMap<String, String>()
                        messageBody.put("message", it.toString())
                        messageBody.put("name", fileUri!!.lastPathSegment!!)
                        messageBody.put("type", checker)
                        messageBody.put("from", messageSenderId)
                        messageBody.put("to", messageReceiverId)
                        messageBody.put("messageID", messagePushId)
                        messageBody.put("time", saveCurrentTime!!)
                        messageBody.put("date", saveCurrentDate!!)

                        var messageBodyDetails = HashMap<String, Any>()
                        messageBodyDetails.put(
                            "$messageSenderRef/$messagePushId",
                            messageBody
                        )

                        messageBodyDetails.put(
                            "$messageReceiverRef/$messagePushId",
                            messageBody
                        )

                        rootRef.updateChildren(messageBodyDetails)
                        dialog.dismiss()
                    }
                }
                    .addOnFailureListener {
                        dialog.dismiss()
                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                    }
                    .addOnProgressListener {
                        val p = (100 * it.bytesTransferred) / it.totalByteCount
                        dialog.setMessage("${p.toInt()} % Uploading...")
                    }


            } else if (checker.equals("image")) {
                val storageReference = FirebaseStorage.getInstance().reference.child("Image Files")

                val messageSenderRef = "Messages/$messageSenderId/$messageReceiverId"
                val messageReceiverRef = "Messages/$messageReceiverId/$messageSenderId"

                val userMsgKeyRef = rootRef.child("Messages")
                    .child(messageSenderId)
                    .child(messageReceiverId)
                    .push()

                val messagePushId = userMsgKeyRef.key
                val filePath = storageReference.child(messagePushId!!)

                uploadTask = filePath.putFile(fileUri!!)
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation filePath.downloadUrl
                })?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val downloadURL = it.result
                        myURL = downloadURL.toString()

                        var messagePictureBody = HashMap<String, String>()
                        messagePictureBody.put("message", myURL)
                        messagePictureBody.put("name", fileUri!!.lastPathSegment!!)
                        messagePictureBody.put("type", checker)
                        messagePictureBody.put("from", messageSenderId)
                        messagePictureBody.put("to", messageReceiverId)
                        messagePictureBody.put("messageID", messagePushId)
                        messagePictureBody.put("time", saveCurrentTime!!)
                        messagePictureBody.put("date", saveCurrentDate!!)


                        var messageBodyDetails = HashMap<String, Any>()
                        messageBodyDetails.put(
                            "$messageSenderRef/$messagePushId",
                            messagePictureBody
                        )

                        messageBodyDetails.put(
                            "$messageReceiverRef/$messagePushId",
                            messagePictureBody
                        )

                        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener {
                            if (it.isSuccessful) {
                                dialog.dismiss()
                                Toast.makeText(
                                    applicationContext,
                                    "Message Sent Successfully",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                dialog.dismiss()
                                Toast.makeText(
                                    applicationContext,
                                    "Error:${it.exception}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            input_message.setText("")
                        }
                    }
                }


            } else {
                dialog.dismiss()
                Toast.makeText(applicationContext, "Nothing selected", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        messageList.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageList.clear()
    }

}
