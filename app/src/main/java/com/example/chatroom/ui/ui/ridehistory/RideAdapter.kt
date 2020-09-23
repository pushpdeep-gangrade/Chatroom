package com.example.chatroom.ui.ui.ridehistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.data.model.CompleteRide
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.ui.ui.rider.DriverViewHolder
import com.google.android.gms.maps.model.LatLng

class RideAdapter(private val list: List<CompleteRide>, private val view: View)
    : RecyclerView.Adapter<RideViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return RideViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride: CompleteRide = list[position]
        holder.bind(ride, view)
    }
}