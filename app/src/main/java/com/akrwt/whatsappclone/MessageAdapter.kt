package com.akrwt.whatsappclone

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private var userMessagesList: ArrayList<Messages>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var mAuth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null


    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var senderMessageText: TextView = itemView.findViewById(R.id.sender_message_text)
        internal var receiverMessageText: TextView =
            itemView.findViewById(R.id.receiver_message_text)
        internal var receiverProfileImage: CircleImageView =
            itemView.findViewById(R.id.message_profile_image)
        internal var messageSenderPicture: ImageView =
            itemView.findViewById(R.id.message_sender_image_view)
        internal var messageReceiverPicture: ImageView =
            itemView.findViewById(R.id.message_receiver_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_messages_layout, parent, false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)

    }

    override fun getItemCount(): Int {
        return userMessagesList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val messageSenderId = mAuth!!.currentUser!!.uid
        val messages = userMessagesList.get(position)
        val fromUserId = messages.getFrom()
        val fromMessageType = messages.getType()

        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserId)
        usersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("image")) {
                    var receiverImage = p0.child("image").getValue().toString()
                    Picasso.get()
                        .load(receiverImage)
                        .placeholder(R.drawable.profile_image)
                        .into(holder.receiverProfileImage)
                }

                holder.receiverMessageText.visibility = View.GONE
                holder.receiverProfileImage.visibility = View.GONE
                holder.senderMessageText.visibility = View.GONE

                holder.messageSenderPicture.visibility = View.GONE
                holder.messageReceiverPicture.visibility = View.GONE

                if (fromMessageType.equals("text")) {

                    if (fromUserId.equals(messageSenderId)) {
                        holder.senderMessageText.visibility = View.VISIBLE
                        holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout)
                        holder.senderMessageText.setTextColor(Color.BLACK)
                        holder.senderMessageText.setText("${messages.getMessage()}\n\n${messages.getTime()} - ${messages.getDate()}")
                    } else {
                        holder.receiverProfileImage.visibility = View.VISIBLE
                        holder.receiverMessageText.visibility = View.VISIBLE
                        holder.senderMessageText.setBackgroundResource(R.drawable.receiver_messages_layout)
                        holder.receiverMessageText.setTextColor(Color.BLACK)
                        holder.receiverMessageText.setText("${messages.getMessage()}\n\n${messages.getTime()} - ${messages.getDate()}")
                    }
                } else if (fromMessageType.equals("image")) {
                    if (fromUserId.equals(messageSenderId)) {
                        Picasso.get()
                            .load(messages.getMessage())
                            .into(holder.messageSenderPicture)
                        holder.messageSenderPicture.visibility = View.VISIBLE
                    } else {
                        holder.receiverProfileImage.visibility = View.VISIBLE
                        Picasso.get()
                            .load(messages.getMessage())
                            .into(holder.messageReceiverPicture)
                        holder.messageReceiverPicture.visibility = View.VISIBLE
                    }
                } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
                    if (fromUserId.equals(messageSenderId)) {
                        holder.messageSenderPicture.visibility = View.VISIBLE

                        Picasso.get()
                            .load("https://firebasestorage.googleapis.com/v0/b/whatsappclone-6cf1f.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=22b07861-714b-408f-96b9-2a64527027a6")
                            .into(holder.messageSenderPicture)

                    } else {

                        holder.receiverProfileImage.visibility = View.VISIBLE
                        holder.messageReceiverPicture.visibility = View.VISIBLE

                        Picasso.get()
                            .load("https://firebasestorage.googleapis.com/v0/b/whatsappclone-6cf1f.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=22b07861-714b-408f-96b9-2a64527027a6")
                            .into(holder.messageReceiverPicture)

                    }
                }
            }
        })
        if (fromUserId.equals(messageSenderId)) {
            holder.itemView.setOnClickListener {
                if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(
                        position
                    ).getType().equals("docx")
                ) {

                    val options = arrayOf(
                        "Delete for me",
                        "Download and view this document",
                        "Cancel",
                        "Delete for everyone"
                    )

                    var builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                deleteSentMessage(position, holder)
                            }
                            1 -> {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(userMessagesList.get(position).getMessage())
                                )
                                holder.itemView.context.startActivity(intent)

                            }
                            3 -> {
                                deleteMessageForEveryone(position, holder)
                            }
                        }

                    }

                    builder.show()

                } else if (userMessagesList.get(position).getType().equals("text")) {

                    val options = arrayOf(
                        "Delete for me",
                        "Cancel",
                        "Delete for everyone"
                    )

                    var builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                deleteSentMessage(position, holder)


                            }
                            2 -> {
                                deleteMessageForEveryone(position, holder)

                            }
                        }
                    }

                    builder.show()


                } else if (userMessagesList.get(position).getType().equals("image")
                ) {

                    val options = arrayOf(
                        "Delete for me",
                        "View this image",
                        "Cancel",
                        "Delete for everyone"
                    )

                    var builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                deleteSentMessage(position, holder)

                            }
                            1 -> {
                                val i =
                                    Intent(holder.itemView.context, ImageViewerActivity::class.java)
                                i.putExtra("url", userMessagesList.get(position).getMessage())
                                holder.itemView.context.startActivity(i)
                            }

                            3 -> {
                                deleteMessageForEveryone(position, holder)

                            }
                        }

                    }

                    builder.show()

                }
            }
        } else {
            holder.itemView.setOnClickListener {
                if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(
                        position
                    ).getType().equals("docx")
                ) {

                    val options = arrayOf(
                        "Delete for me",
                        "Download and view this document",
                        "Cancel"
                    )

                    var builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                deleteReceiveMessage(position, holder)

                            }
                            1 -> {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(userMessagesList.get(position).getMessage())
                                )
                                holder.itemView.context.startActivity(intent)

                            }

                        }

                    }

                    builder.show()

                } else if (userMessagesList.get(position).getType().equals("text")) {

                    val options = arrayOf(
                        "Delete for me",
                        "Cancel"
                    )

                    var builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> {

                                deleteReceiveMessage(position, holder)
                            }


                        }

                    }

                    builder.show()

                } else if (userMessagesList.get(position).getType().equals("image")
                ) {

                    val options = arrayOf(
                        "Delete for me",
                        "View this image",
                        "Cancel"
                    )

                    var builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                deleteReceiveMessage(position, holder)

                            }
                            1 -> {
                                val i =
                                    Intent(holder.itemView.context, ImageViewerActivity::class.java)
                                i.putExtra("url", userMessagesList.get(position).getMessage())
                                holder.itemView.context.startActivity(i)

                            }
                        }

                    }

                    builder.show()

                }
            }

        }
    }

    fun deleteSentMessage(position: Int, holder: MessageViewHolder) {

        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages")
            .child(userMessagesList.get(position).getFrom())
            .child(userMessagesList.get(position).getTo())
            .child(userMessagesList.get(position).getMessageID())
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Deleted Successfully",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error : ${it.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

    }

    fun deleteReceiveMessage(position: Int, holder: MessageViewHolder) {

        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages")
            .child(userMessagesList.get(position).getTo())
            .child(userMessagesList.get(position).getFrom())
            .child(userMessagesList.get(position).getMessageID())
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Deleted Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error : ${it.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun deleteMessageForEveryone(position: Int, holder: MessageViewHolder) {

        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages")
            .child(userMessagesList.get(position).getTo())
            .child(userMessagesList.get(position).getFrom())
            .child(userMessagesList.get(position).getMessageID())
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    rootRef.child("Messages")
                        .child(userMessagesList.get(position).getFrom())
                        .child(userMessagesList.get(position).getTo())
                        .child(userMessagesList.get(position).getMessageID())
                        .removeValue().addOnCompleteListener {
                            Toast.makeText(
                                holder.itemView.context,
                                "Deleted Successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error : ${it.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

}