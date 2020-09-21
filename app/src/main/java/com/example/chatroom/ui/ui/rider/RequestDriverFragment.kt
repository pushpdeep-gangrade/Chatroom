package com.example.chatroom.ui.ui.rider

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentRequestDriverBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.ActiveUserAdapter
import com.example.chatroom.ui.ui.chatroom.chatRoomId

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import org.json.JSONObject


class RequestDriverFragment : Fragment() {
    private var _binding: FragmentRequestDriverBinding? = null
    private val binding get() = _binding!!
    private var activeUsers = mutableListOf<MapUser>()
    private var requestId: String? = null
    private var pickupLatitude: Double = 0.0
    private var pickupLongitude: Double = 0.0
    private lateinit var pickupLocationLatLng: LatLng


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //chatRoomId = arguments?.getString("chatroomId").toString()
        requestId = arguments?.getString("requestId").toString()
        pickupLatitude = arguments?.getString("pickupLatitude")!!.toDouble()
        pickupLongitude = arguments?.getString("pickupLongitude")!!.toDouble()

        pickupLocationLatLng = LatLng(pickupLatitude, pickupLongitude)
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
                            activeUsers.add(u)

                        }
                    }

                    updateActiveUsers()

                    for (ac in activeUsers) {
                        Log.d("check active user ", "${ac.rider.firstName}")
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
        binding.driverRecyclerView.adapter = DriverAdapter(activeUsers, view, requestId.toString(),
            pickupLocationLatLng
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("demo", "Destroy: User is no longer active")
        Log.d("demo", "${chatRoomId}: ${requestId}")
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("rideRequests")
            .child(requestId.toString()).removeValue()
    }
}