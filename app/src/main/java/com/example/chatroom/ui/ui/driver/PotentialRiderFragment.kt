package com.example.chatroom.ui.ui.driver

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.databinding.FragmentChatroomBinding
import com.example.chatroom.databinding.FragmentPotentialRiderBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.example.chatroom.ui.ui.chatroom.messageUser

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso

class PotentialRiderFragment : Fragment() {
    private var _binding : FragmentPotentialRiderBinding? = null
    private val binding get() = _binding!!
    var requestId: String = ""
    var requestInfo : RideRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //chatRoomId = arguments?.getString("chatroomId")
        Log.d("Pass RId", "Before")

        requestId = arguments?.getString("requestId").toString()

        Log.d("Pass RId", requestId.toString())
        Log.d("Pass CId", chatRoomId.toString())

        _binding = FragmentPotentialRiderBinding.inflate(inflater, container, false)

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("rideRequests").child(requestId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("demo", "Firebase event cancelled on getting user data")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                requestInfo = dataSnapshot.getValue<RideRequest>()

                Log.d("Request Info", requestInfo?.riderInfo?.email.toString())

                Picasso.get().load(requestInfo?.riderInfo?.imageUrl.toString()).resize(250, 250).into(binding.potentialRiderImageview)
                _binding!!.potentialRiderFirstnameTextview.text = requestInfo?.riderInfo?.firstName.toString()
                _binding!!.potentialRiderLastnameTextview.text = requestInfo?.riderInfo?.lastName.toString()
                _binding!!.potentialRiderPickupLocationTextview.text = requestInfo?.pickupLocation?.name.toString()
                _binding!!.potentialRiderDropoffLocationTextview.text = requestInfo?.dropoffLocation?.name.toString()
            }
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Button", "Butto Clicked")

      /*  Picasso.get().load(requestInfo?.riderInfo?.imageUrl.toString()).resize(250, 250).into(binding.potentialRiderImageview)
        binding.potentialRiderFirstnameTextview.text = requestInfo?.riderInfo?.firstName.toString()
        binding.potentialRiderLastnameTextview.text = requestInfo?.riderInfo?.lastName.toString()
        binding.potentialRiderPickupLocationTextview.text = requestInfo?.pickupLocation?.name.toString()
        binding.potentialRiderDropoffLocationTextview.text = requestInfo?.dropoffLocation?.name.toString()
*/
        binding.acceptRequestButton.setOnClickListener {
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests").child(requestId).child("drivers").child(MainActivity.auth.currentUser?.uid.toString())
                .setValue(messageUser)

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests").child(requestId)
                .child("status").setValue("Available")

            val bundle = bundleOf("requestId" to requestId)
            view.findNavController().navigate(R.id.action_nav_potential_rider_to_nav_wait_for_accept, bundle)
        }

        binding.rejectRequestButton.setOnClickListener {
            val bundle1 = Bundle()
            bundle1.putString("chatroomId", chatRoomId.toString())
            view.findNavController().navigate(R.id.action_nav_potential_rider_to_chatroom, bundle1)
        }
    }
}