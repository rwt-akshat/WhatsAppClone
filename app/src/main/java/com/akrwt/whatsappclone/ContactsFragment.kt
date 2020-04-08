package com.akrwt.whatsappclone


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ContactsFragment : Fragment() {

    lateinit var ContactsView: View
    lateinit var myContactsList: RecyclerView
    lateinit var ContactsRef: DatabaseReference
    lateinit var UsersRef: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false)
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid

        ContactsRef =
            FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserId)
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")

        myContactsList = ContactsView.findViewById(R.id.contacts_list)
        myContactsList.layoutManager = LinearLayoutManager(context)

        return ContactsView
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(ContactsRef, Contacts::class.java)
            .build()

        val adapter = object :
            FirebaseRecyclerAdapter<Contacts, ContactsFragment.ContactsViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ContactsFragment.ContactsViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_display_layout, parent, false)
                return ContactsFragment.ContactsViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: ContactsFragment.ContactsViewHolder,
                position: Int,
                model: Contacts
            ) {
                var userIds = getRef(position).key
                UsersRef.child(userIds!!).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                    if(p0.exists()){

                        if(p0.child("userState").hasChild("state")){
                            var state=p0.child("userState").child("state").getValue().toString()
                            var date=p0.child("userState").child("date").getValue().toString()
                            var time=p0.child("userState").child("time").getValue().toString()

                            if(state.equals("online")){
                                holder.onlineStatus.visibility=View.VISIBLE
                            }else if(state.equals("offline")){
                                holder.onlineStatus.visibility=View.INVISIBLE

                            }

                        }
                        else{
                            holder.onlineStatus.visibility=View.INVISIBLE

                        }


                        if (p0.hasChild("image")) {
                            val profileImage = p0.child("image").getValue().toString()
                            val profileName = p0.child("name").getValue().toString()
                            val profileStatus = p0.child("status").getValue().toString()

                            holder.userName.text = profileName
                            holder.userStatus.text = profileStatus

                            Picasso.get()
                                .load(profileImage)
                                .placeholder(R.drawable.profile_image)
                                .into(holder.profileImage)
                        }
                        else{
                            val profileName = p0.child("name").getValue().toString()
                            val profileStatus = p0.child("status").getValue().toString()

                            holder.userName.text = profileName
                            holder.userStatus.text = profileStatus

                        }
                    }
                    }
                })
            }
        }
        myContactsList.adapter = adapter
        adapter.startListening()
    }

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var userName: TextView = itemView.findViewById(R.id.user_profile_name)
        internal var userStatus: TextView = itemView.findViewById(R.id.user_status)
        internal var profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
        internal var onlineStatus:ImageView=itemView.findViewById(R.id.user_online_status)
    }


}
