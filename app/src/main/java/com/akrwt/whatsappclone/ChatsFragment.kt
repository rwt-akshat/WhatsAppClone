package com.akrwt.whatsappclone


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class ChatsFragment : Fragment() {

    lateinit var privateChatsView: View
    lateinit var chatsList: RecyclerView
    lateinit var ChatsRef: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUserId: String
    lateinit var UsersRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false)
        chatsList = privateChatsView.findViewById(R.id.chats_list)
        chatsList.layoutManager = LinearLayoutManager(context)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid

        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")
        ChatsRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserId)
        return privateChatsView
    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(ChatsRef, Contacts::class.java)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ChatsViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_display_layout, parent, false)
                return ChatsViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: ChatsViewHolder,
                position: Int,
                model: Contacts
            ) {
                var usersId = getRef(position).key
                var retImage: String = "default_image"


                UsersRef.child(usersId!!).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if (p0.exists()) {

                            if (p0.hasChild("image")) {
                                retImage = p0.child("image").value.toString()
                                Picasso.get()
                                    .load(retImage)
                                    .into(holder.profileImage)
                            }

                            val retName = p0.child("name").value.toString()
                          //  val retStatus = p0.child("status").value.toString()

                            holder.userName.setText(retName)
                            holder.userStatus.setText("Last Seen: \n Date Time")

                            if(p0.child("userState").hasChild("state")){
                                var state=p0.child("userState").child("state").getValue().toString()
                                var date=p0.child("userState").child("date").getValue().toString()
                                var time=p0.child("userState").child("time").getValue().toString()

                                if(state.equals("online")){
                                    holder.userStatus.setText("online")
                                }else if(state.equals("offline")){
                                    holder.userStatus.setText("Last Seen: $date  $time")

                                }

                            }
                            else{
                                holder.userStatus.setText("offline")
                            }


                            holder.itemView.setOnClickListener {
                                val i = Intent(context, ChatActivity::class.java)
                                i.putExtra("visit_user_id", usersId)
                                i.putExtra("visit_user_name", retName)
                                i.putExtra("visit_image",retImage)
                                startActivity(i)
                            }
                        }
                    }
                })
            }
        }

        chatsList.adapter = adapter
        adapter.startListening()

    }

    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var userName: TextView = itemView.findViewById(R.id.user_profile_name)
        internal var userStatus: TextView = itemView.findViewById(R.id.user_status)
        internal var profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
    }


}
