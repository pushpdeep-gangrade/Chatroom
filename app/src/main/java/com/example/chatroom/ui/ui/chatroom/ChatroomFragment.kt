package com.example.chatroom.ui.ui.chatroom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentChatroomsBinding
import com.example.chatroom.databinding.FragmentProfileBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.UpdateProfile
import com.example.chatroom.ui.ui.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.auth.User
import com.squareup.picasso.Picasso

class ChatroomFragment : Fragment() {
    private var _binding : FragmentChatroomsBinding ? = null
    private val binding get() = _binding!!
    private lateinit var chatroomViewModel: ChatroomViewModel
    val chatrooms = mutableListOf<String>()

    // access the listView from xml file
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatroomViewModel =
            ViewModelProviders.of(this).get(ChatroomViewModel::class.java)
        _binding = FragmentChatroomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateAdapter()

    }

    fun updateAdapter(){
            MainActivity.dbRef.child("chatrooms").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnapshot in dataSnapshot.children) {
                   chatrooms.add(postSnapshot.key.toString())
                    }
                    val arrayAdapter =
                        context?.let { ArrayAdapter<String>(it,android.R.layout.simple_list_item_1,chatrooms ) }
                    binding.listView.adapter = arrayAdapter
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

}