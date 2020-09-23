package com.example.chatroom.ui.ui.ridehistory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.example.chatroom.data.model.CompleteRide
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso

class RideViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.ride_item, parent, false)) {

    private var ridePickupLocation : TextView? = null
    private var rideDropoffLocation: TextView? = null
    private var rideRiderName: TextView? = null
    private var rideDriverName: TextView? = null

    init {
        ridePickupLocation = itemView.findViewById(R.id.rideItem_pickUpLocation)
        rideDropoffLocation = itemView.findViewById(R.id.rideItem_dropoffLocation)
        rideRiderName  = itemView.findViewById(R.id.rideItem_rider)
        rideDriverName= itemView.findViewById(R.id.rideItem_driver)
    }

    fun bind(ride : CompleteRide, view: View) {
        ridePickupLocation?.text = ride.rideRequest.pickupLocation.name
        rideDropoffLocation?.text = ride.rideRequest.dropoffLocation.name
        rideRiderName?.text = "Rider: ".plus(ride.rideRequest.riderInfo.firstName).plus(" ").plus(ride.rideRequest.riderInfo.lastName)
        rideDriverName?.text = "Driver: ".plus(ride.driver.driver.firstName).plus(" ").plus(ride.driver.driver.lastName)

        itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("requestId", ride.rideRequest.requestId)
            view.findNavController().navigate(R.id.action_nav_ride_history_to_nav_ride_details, bundle)
        }
    }
}