package com.example.chatroom.ui.ui.rider

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatroom.R
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentRequestDriverBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.ActiveUserAdapter
import com.example.chatroom.ui.ui.chatroom.chatRoomId

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class RequestDriverFragment : Fragment() {
    private var _binding : FragmentRequestDriverBinding? = null
    private val binding get() = _binding!!
    private var activeUsers  = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRequestDriverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getActiveUsers()

    }

    fun getActiveUsers(){
        Log.d("Current Chatroom", chatRoomId.toString())
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                activeUsers.clear()
                for (postSnapshot in dataSnapshot.children) {
                    var u : User? = postSnapshot.getValue<User>()
                    if (u != null) {
                        activeUsers.add(u)

                    }
                }

                updateActiveUsers()

                for(ac in activeUsers){
                    Log.d("check active user " , "${ac.firstName}")
                }


            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("demoo", "cancel")
            }
        })
    }

    fun updateActiveUsers(){
        binding.driverRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)
        binding.driverRecyclerView.adapter = DriverAdapter(activeUsers, view)
    }

}