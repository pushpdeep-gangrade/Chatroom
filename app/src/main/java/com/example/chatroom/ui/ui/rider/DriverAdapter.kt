package com.example.chatroom.ui.ui.rider

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.example.chatroom.data.model.CompleteRide
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.data.model.User
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.Chat
import com.example.chatroom.ui.ui.chatroom.ChatViewHolder
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.serialization.json.Json.Default.context

class DriverAdapter(private val list: List<MapUser>, private val view: View?,
                    private val requestId: String, private val pickupLocationLatLng: LatLng,
                    private val dropoffLocationLatLng: LatLng)
    : RecyclerView.Adapter<DriverViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        context = parent.context
        return DriverViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        val driver: MapUser = list[position]
        holder.bind(driver, view, requestId, context, pickupLocationLatLng, dropoffLocationLatLng, list)
        holder.itemView.setOnClickListener{
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests")
                .child(requestId).child("drivers").child(driver.driver.userId)
                .child("status").setValue("Accepted")

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("rideRequests")
                .child(requestId.toString()).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("demo", "Firebase event cancelled on getting user data")
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var ride: RideRequest? = dataSnapshot.getValue<RideRequest>()
                        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests")
                            .child(requestId).child("ride").setValue(ride)

                        var completeRide: CompleteRide = CompleteRide(ride!!, driver)

                        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                            .child("activeRides").child(requestId).setValue(completeRide)


                    }
                })

            val bundle = Bundle()
            bundle.putString("rideId", requestId)
            view?.findNavController()?.navigate(R.id.action_nav_request_driver_to_nav_waiting_on_ride, bundle)
        }
    }

    fun acceptDriver(){

    }
}