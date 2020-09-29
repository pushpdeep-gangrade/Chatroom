package com.example.chatroom.ui.ui.ridehistory

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatroom.R
import com.example.chatroom.data.model.CompleteRide
import com.example.chatroom.data.model.PickedPlace
import com.example.chatroom.databinding.FragmentPotentialRiderBinding
import com.example.chatroom.databinding.FragmentRideHistoryBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.*
import com.example.chatroom.ui.ui.rider.DriverAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class RideHistoryFragment : Fragment() {
    private var _binding : FragmentRideHistoryBinding? = null
    private val binding get() = _binding!!
    private var listCompletedRides : MutableList<CompleteRide> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRideHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MainActivity.dbRef.child("rideHistory").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                listCompletedRides.clear()

                for (postSnapshot in dataSnapshot.children) {
                    var cr : CompleteRide? = postSnapshot.getValue<CompleteRide>()
                    if (cr != null) {
                        if(cr.driver.driver.userId == FirebaseAuth.getInstance().currentUser?.uid.toString() || cr.rideRequest.riderInfo.userId == FirebaseAuth.getInstance().currentUser?.uid.toString()){
                            listCompletedRides.add(cr)
                            Log.d("Completed Rides", cr.toString())
                        }
                    }
                }

                updateRideHistory(view)

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("demoo", "cancel")
            }
        })
    }

    fun updateRideHistory(view: View) {
        binding.rideHistoryRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rideHistoryRecyclerView.adapter = RideAdapter(listCompletedRides, view)
    }

}