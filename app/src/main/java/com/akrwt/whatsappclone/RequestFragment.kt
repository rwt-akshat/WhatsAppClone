package com.akrwt.whatsappclone


import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.users_display_layout.*

class RequestFragment : Fragment() {

    lateinit var RequestFragmentView: View
    lateinit var myRequestList: RecyclerView
    lateinit var chatreqRef: DatabaseReference
    lateinit var userRef: DatabaseReference
    lateinit var ContactsRef: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        RequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false)

        chatreqRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        ContactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid


        myRequestList = RequestFragmentView.findViewById(R.id.chat_request_list)
        myRequestList.layoutManager = LinearLayoutManager(context)

        return RequestFragmentView
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(chatreqRef.child(currentUserId), Contacts::class.java)
            .build()

        val adapter = object :
            FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RequestViewHolder {

                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_display_layout, parent, false)

                return RequestViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: RequestViewHolder,
                position: Int,
                model: Contacts
            ) {
                holder.acceptBtn.visibility = View.VISIBLE
                holder.cancelBtn.visibility = View.VISIBLE
                var list_user_id = getRef(position).key
                var getTypeRef = getRef(position).child("request_type").ref
                getTypeRef.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            var type = p0.value.toString()
                            if (type.equals("received")) {
                                userRef.child(list_user_id!!)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {

                                        }

                                        @TargetApi(Build.VERSION_CODES.N)
                                        override fun onDataChange(p0: DataSnapshot) {
                                            if (p0.hasChild("image")) {

                                                var reqUserImage =
                                                    p0.child("image").getValue().toString()

                                                Picasso.get()
                                                    .load(reqUserImage)
                                                    .into(holder.profileImage)

                                            }

                                            val reqUserName = p0.child("name").getValue().toString()
                                            //val reqUserStatus = p0.child("status").getValue().toString()

                                            holder.userName.setText(reqUserName)
                                            holder.userStatus.setText("Wants to connect with you.")


                                            holder.acceptBtn.setOnClickListener {
                                                val builder = AlertDialog.Builder(context)
                                                builder.setTitle("$reqUserName Chat Request")
                                                builder.setMessage("Do you really want to accept the request.")
                                                builder.setPositiveButton(
                                                    Html.fromHtml(
                                                        "<font color='#FF7F27'>Yes</font>",
                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                                    )
                                                ) { dialog: DialogInterface?, which: Int ->
                                                    ContactsRef.child(currentUserId)
                                                        .child(list_user_id).child("Contact")
                                                        .setValue("Saved").addOnCompleteListener {
                                                            if (it.isSuccessful) {
                                                                ContactsRef.child(list_user_id)
                                                                    .child(currentUserId)
                                                                    .child("Contact")
                                                                    .setValue("Saved")
                                                                    .addOnCompleteListener {
                                                                        if (it.isSuccessful) {

                                                                            chatreqRef.child(
                                                                                currentUserId
                                                                            ).child(list_user_id)
                                                                                .removeValue()
                                                                                .addOnCompleteListener {

                                                                                    if (it.isSuccessful) {
                                                                                        chatreqRef.child(
                                                                                            list_user_id
                                                                                        ).child(
                                                                                            currentUserId
                                                                                        )
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener {

                                                                                                if (it.isSuccessful)
                                                                                                    Toast.makeText(
                                                                                                        context,
                                                                                                        "New Contact Added",
                                                                                                        Toast.LENGTH_LONG
                                                                                                    ).show()

                                                                                            }
                                                                                    }
                                                                                }
                                                                        }
                                                                    }
                                                            }
                                                        }
                                                }
                                                builder.setNegativeButton(
                                                    Html.fromHtml(
                                                        "<font color='#FF7F27'>No</font>",
                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                                    )
                                                ) { dialog: DialogInterface?, which: Int ->

                                                }
                                                builder.create().show()
                                            }

                                            holder.cancelBtn.setOnClickListener {
                                                val builder = AlertDialog.Builder(context)
                                                builder.setTitle("$reqUserName Chat Request")
                                                builder.setMessage("Do you really want to cancel the request.")
                                                builder.setPositiveButton(
                                                    Html.fromHtml(
                                                        "<font color='#FF7F27'>Yes</font>",
                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                                    )
                                                ) { dialog: DialogInterface?, which: Int ->

                                                    chatreqRef.child(currentUserId)
                                                        .child(list_user_id)
                                                        .removeValue().addOnCompleteListener {

                                                            if (it.isSuccessful) {
                                                                chatreqRef.child(list_user_id)
                                                                    .child(currentUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener {

                                                                        if (it.isSuccessful)
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Contact Deleted",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()

                                                                    }
                                                            }
                                                        }
                                                }
                                                builder.setNegativeButton(
                                                    Html.fromHtml(
                                                        "<font color='#FF7F27'>No</font>",
                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                                    )
                                                ) { dialog: DialogInterface?, which: Int ->

                                                }

                                                builder.create().show()
                                            }
                                        }
                                    })

                            } else if (type.equals("sent")) {

                                holder.acceptBtn.setText("Request Sent")
                                holder.cancelBtn.visibility = View.INVISIBLE

                                userRef.child(list_user_id!!)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                        }

                                        @TargetApi(Build.VERSION_CODES.N)
                                        override fun onDataChange(p0: DataSnapshot) {
                                            if (p0.hasChild("image")) {

                                                var reqUserImage =
                                                    p0.child("image").getValue().toString()

                                                Picasso.get()
                                                    .load(reqUserImage)
                                                    .into(holder.profileImage)

                                            }

                                            val reqUserName = p0.child("name").getValue().toString()
                                            //val reqUserStatus = p0.child("status").getValue().toString()

                                            holder.userName.setText(reqUserName)
                                            holder.userStatus.setText("You have sent a request to $reqUserName")


                                            holder.acceptBtn.setOnClickListener {
                                                val builder = AlertDialog.Builder(context)
                                                builder.setTitle("Already sent request")
                                                builder.setMessage("Do you really want to cancel the request.")

                                                builder.setPositiveButton(
                                                    Html.fromHtml(
                                                        "<font color='#FF7F27'>Yes</font>",
                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                                    )
                                                ) { dialog: DialogInterface?, which: Int ->

                                                    chatreqRef.child(currentUserId)
                                                        .child(list_user_id)
                                                        .removeValue().addOnCompleteListener {

                                                            if (it.isSuccessful) {
                                                                chatreqRef.child(list_user_id)
                                                                    .child(currentUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener {

                                                                        if (it.isSuccessful)
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Chat request deleted.",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()
                                                                    }
                                                            }
                                                        }
                                                }
                                                builder.setNegativeButton(
                                                    Html.fromHtml(
                                                        "<font color='#FF7F27'>No</font>",
                                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                                    )
                                                ) { dialog: DialogInterface?, which: Int ->

                                                }
                                                builder.create().show()
                                            }
                                        }
                                    })
                            }
                        }
                    }
                })
            }
        }
        myRequestList.adapter = adapter
        adapter.startListening()

    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var userName: TextView = itemView.findViewById(R.id.user_profile_name)
        internal var userStatus: TextView = itemView.findViewById(R.id.user_status)
        internal var profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
        internal var acceptBtn: Button = itemView.findViewById(R.id.request_accept_btn)
        internal var cancelBtn: Button = itemView.findViewById(R.id.request_cancel_btn)
    }
}
