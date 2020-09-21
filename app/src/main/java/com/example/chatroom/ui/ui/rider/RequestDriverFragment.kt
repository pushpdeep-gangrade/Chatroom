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
import com.example.chatroom.data.model.MapUser
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
    private var _binding: FragmentRequestDriverBinding? = null
    private val binding get() = _binding!!
    private var activeUsers = mutableListOf<User>()
    private var requestId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //chatRoomId = arguments?.getString("chatroomId").toString()
        requestId = arguments?.getString("requestId").toString()
        Log.d("Pass RId Driver", requestId.toString())
        getDrivers()



        _binding = FragmentRequestDriverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDrivers()

    }

    fun getDrivers() {
        Log.d("Current Chatroom", chatRoomId.toString())
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests")
            .child(requestId.toString()).child("drivers").addValueEventListener(object :
            //MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests").child("drivers").addValueEventListener(object :

                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    activeUsers.clear()
                    for (postSnapshot in dataSnapshot.children) {
                        var u: MapUser? = postSnapshot.getValue<MapUser>()
                        if (u != null) {
                            activeUsers.add(u.rider)

                        }
                    }

                    updateActiveUsers()

                    for (ac in activeUsers) {
                        Log.d("check active user ", "${ac.firstName}")
                    }


                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demoo", "cancel")
                }
            })
    }

    fun updateActiveUsers() {
        binding.driverRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.driverRecyclerView.adapter = DriverAdapter(activeUsers, view, requestId.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("demo", "Destroy: User is no longer active")
        Log.d("demo", "${chatRoomId}: ${requestId}")
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("rideRequests")
            .child(requestId.toString()).removeValue()
    }
}