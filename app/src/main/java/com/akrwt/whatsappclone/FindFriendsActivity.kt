package com.akrwt.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FindFriendsActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var FindFriendsRecyclerList: RecyclerView
    lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)

        usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        FindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list)
        FindFriendsRecyclerList.layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.find_friends_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Find Friends"
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(usersRef, Contacts::class.java)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): FindFriendViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_display_layout, parent, false)
                return FindFriendViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: FindFriendViewHolder,
                position: Int,
                model: Contacts
            ) {
                holder.userName.text = model.getName()
                holder.userStatus.text = model.getStatus()
                if (model.getImage().isEmpty())
                    holder.profileImage.setImageResource(R.drawable.profile_image)
                else
                    Picasso.get()
                        .load(model.getImage())
                        .into(holder.profileImage)

                holder.itemView.setOnClickListener{
                    val visit_user_id=getRef(position).key
                    val i=Intent(this@FindFriendsActivity,ProfileActivity::class.java)
                    i.putExtra("visit_user_id",visit_user_id)
                    startActivity(i)
                }
            }
        }

        FindFriendsRecyclerList.adapter = adapter
        adapter.startListening()
    }

    class FindFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var userName: TextView = itemView.findViewById(R.id.user_profile_name)
        internal var userStatus: TextView = itemView.findViewById(R.id.user_status)
        internal var profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
    }
}
