package com.akrwt.whatsappclone


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_group.*

class GroupFragment : Fragment() {

    private var groupFragmentView: View? = null
    private var list_View: ListView? = null
    private var arrayAdapter: ArrayAdapter<String>? = null
    private var list_of_groups = mutableListOf<String>()

    private var GroupRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        groupFragmentView = inflater.inflate(R.layout.fragment_group, container, false)
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups")

        initializeFields()

        retrieveAndDisplayGrps()


        list_View!!.setOnItemClickListener{ adapterView: AdapterView<*>, view: View, position: Int, id: Long ->

            var currentGrpName=adapterView.getItemAtPosition(position).toString()
            var i =Intent(context,GroupChatActivity::class.java)
            i.putExtra("groupName",currentGrpName)
            startActivity(i)



        }


        return groupFragmentView
    }

    private fun initializeFields() {
        list_View = groupFragmentView!!.findViewById(R.id.list_view)
        arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, list_of_groups)
        list_View!!.adapter = arrayAdapter
    }

    private fun retrieveAndDisplayGrps() {

        GroupRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val set = HashSet<String>()
                val iterator = dataSnapshot.children.iterator()
                while (iterator.hasNext()) {
                    set.add(((iterator.next()).key)!!)
                }
                list_of_groups.clear()
                list_of_groups.addAll(set)
                arrayAdapter!!.notifyDataSetChanged()
            }
        })
    }
}
