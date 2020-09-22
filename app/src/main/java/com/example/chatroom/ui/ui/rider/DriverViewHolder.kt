package com.example.chatroom.ui.ui.rider

import android.content.Context
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.*
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.example.chatroom.ui.ui.chatroom.messageUser
import com.example.chatroom.ui.ui.chatroom.messageUserId
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.*

class DriverViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.driver_item, parent, false)){

    private var driverProfilePic : ImageView? = null
    private var driverFullName: TextView? = null
    private var driverLocation: TextView? = null
    private var driverEta: TextView? = null
    private var yesButton: Button? = null
    private var noButton: Button? = null
    var path: MutableList<List<LatLng>> = ArrayList()
    var urlDirections: String =
        "https://maps.googleapis.com/maps/api/directions/json?"
    var directionsRequest: StringRequest? = null
   

    init {
        driverProfilePic = itemView.findViewById(R.id.driverItem_profilePic)
        driverFullName = itemView.findViewById(R.id.driverItem_fullName)
        driverLocation  = itemView.findViewById(R.id.driverItem_location)
        driverEta= itemView.findViewById(R.id.driverItem_ETA)
        yesButton  = itemView.findViewById(R.id.driverItem_yesButton)
        noButton = itemView.findViewById(R.id.driverItem_noButton)
    }

    fun bind(driver: MapUser, view: View?, requestId: String, context: Context, pickupLocationLatLng: LatLng, dropoffLocationLatLng: LatLng) {
       /* var geocoder : Geocoder = Geocoder(context, Locale.getDefault());
        var addresses : MutableList<Address>
        addresses = geocoder.getFromLocation(driver.lat, driver.long, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val address = addresses[0]
            .getAddressLine(0)
        val city = addresses[0].locality
        val state = addresses[0].adminArea*/


        getDistanceAndTime(pickupLocationLatLng, driver, context)

        driverFullName?.text = driver.driver.firstName.plus(" ").plus(driver.driver.lastName)
        Picasso.get().load(driver.driver.imageUrl).resize(250, 250).into(driverProfilePic)
        //driverLocation!!.text = driver.lat.toString().plus(" ").plus(driver.long.toString())
        //driverLocation!!.text = address

        yesButton?.setOnClickListener {
            Log.d("Yes Button", "Clicked")

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests")
                .child(requestId).child("drivers").child(driver.driver.userId)
                .child("status").setValue("Accepted")


            //var rider = messageUser?.let { it1 -> RequestRideFragment.lastKnownLocation?.latitude?.let { it2 ->
            //    RequestRideFragment.lastKnownLocation?.longitude?.let { it3 ->
            //        MapUser(MainActivity.auth.currentUser?.uid.toString(), it1,
            //            it2, it3
            //        )
            //    }
            //} }


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

            /*MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("rideRequests")
                .child(requestId.toString()).removeValue()*/

            val bundle = Bundle()
            bundle.putString("rideId", requestId)
            view?.findNavController()?.navigate(R.id.action_nav_request_driver_to_nav_waiting_on_ride, bundle)


        }

        noButton?.setOnClickListener {
            Log.d("No Button", "Clicked")

            val bundle = Bundle()
            bundle.putString("chatroomId", chatRoomId.toString())

            view?.findNavController()!!.navigate(R.id.action_nav_request_driver_to_chatroom, bundle)
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests")
                .child(requestId).removeValue()
        }

    }

    fun getDistanceAndTime(pickupLocationLatLng: LatLng, driver: MapUser, context: Context){
        directionsRequest = object : StringRequest(
            Request.Method.GET,
            urlDirections
                .plus("key=${context?.resources?.getString(R.string.api_key)}")
                .plus("&origin=${driver.lat},${driver.long}")
                .plus("&destination=${pickupLocationLatLng.latitude},${pickupLocationLatLng.longitude}"),
            Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
                val duration = legs.getJSONObject(0).getJSONObject("duration").getString("text")

                val distanceText = "Distance Away: ".plus(distance)
                val durationText = "Estimated Time: ".plus(duration)

                driverLocation?.text = distanceText
                driverEta?.text = durationText

                Log.d("Distance", distance)
                Log.d("Time", duration)

            },
            Response.ErrorListener { _ ->
            }) {}
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }
}