package com.example.chatroom.ui.ui.rider

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.databinding.FragmentChatroomsBinding
import com.example.chatroom.databinding.FragmentWaitingOnRideBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.util.ArrayList

private var rideId: String? = null
private var rider: MapUser? = null
private var driver: MapUser? = null
private var map: GoogleMap? = null
private var dropoff : LatLng ? =null
private var directionsRequest: StringRequest? = null
private var urlDirections: String = "https://maps.googleapis.com/maps/api/directions/json?"
private var path: MutableList<List<LatLng>> = ArrayList()
private var polyline : Polyline ?=null


class WaitingOnRideFragment : Fragment(), OnMapReadyCallback {
    private var car_location : Marker ?= null
    private var _binding: FragmentWaitingOnRideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rideId = arguments?.getString("rideId")
        Log.d("demo", "check ride id" + rideId)
        _binding = FragmentWaitingOnRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()

        setDriverArrivedOrCanceledListener(view)

        binding.waitingOnRideCancelButton.setOnClickListener {
                      val bundle = Bundle()
            bundle.putString("chatroomId", chatRoomId.toString())

            view.findNavController().navigate(R.id.action_nav_waiting_on_ride_to_chatroom, bundle)
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests")
                .child(rideId!!).removeValue()

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("activeRides")
                .child(rideId!!).child("driver").child("status").setValue("RiderCanceled")

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("activeRides")
                .child(rideId!!).removeValue()
        }


    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        getLocation()
    }

    fun initialize() {
        val mapFragment: SupportMapFragment? =
            FragmentManager.findFragment(view?.findViewById(R.id.waitingOnRide_mapView)!!) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

    }

    fun getLocation() {
        if (rideId != null) {
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests")
                .child(rideId!!).child("ride").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val curent_ride = dataSnapshot.getValue<RideRequest>()


                        if (curent_ride != null) {
                            rider = MapUser(curent_ride.riderInfo, curent_ride.pickupLocation.latitude, curent_ride.pickupLocation.longitude, curent_ride.status)
                            dropoff = LatLng(curent_ride?.dropoffLocation.latitude, curent_ride?.dropoffLocation.longitude)
                        }

                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests")
                .child(rideId!!).child("drivers")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (postSnapshot in dataSnapshot.children) {
                            driver = postSnapshot.getValue<MapUser>()
                            if(driver?.status.equals("Accepted"))
                                driver = postSnapshot.getValue<MapUser>()
                        }
                        if (context != null) {
                            updateLocationUI()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })

        }
    }

    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        map?.clear()
        val riderLocation = rider?.lat?.let { rider?.long?.let { it1 -> LatLng(it, it1) } }
        val driverLocation = driver?.lat?.let { driver?.long?.let { it1 -> LatLng(it, it1) } }

        map?.addMarker(riderLocation?.let {
            MarkerOptions().position(it).title(rider?.driver?.firstName).icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_marker))
        })


        var a = Location("rider")
        var b = Location("drive")

        a.latitude = riderLocation?.latitude!!
        a.longitude = riderLocation?.longitude!!

        b.latitude =driverLocation?.latitude!!
        b.longitude= driverLocation?.longitude!!

        if(a.distanceTo(b) <= 200){
            Toast.makeText(context, "Driver is here", Toast.LENGTH_LONG).show()
          //  Log.d("distance",a.distanceTo(b).toString())
        }


        car_location = map?.addMarker(MarkerOptions().position(driverLocation!!).title(driver?.driver?.firstName).icon(BitmapDescriptorFactory.fromResource(R.drawable.driver_icon)))

        map?.addMarker(driverLocation?.let {
            dropoff?.let { it1 -> MarkerOptions().position(it1).title("Dropoff") }
        })

        polyline?.remove()
        path.clear()
        directionsRequest = object : StringRequest(
            Request.Method.GET,
            urlDirections
                .plus("key=${context?.resources?.getString(R.string.api_key)}")
                .plus("&origin=${driverLocation?.latitude},${driverLocation?.longitude}")
                .plus("&destination=${riderLocation?.latitude},${riderLocation?.longitude}"),
            Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline")
                        .getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {

             polyline = map!!.addPolyline(
                        PolylineOptions().addAll(path[i]).color(Color.RED)
                    )

                }
                // Get bounds
                val bounds = routes.getJSONObject(0).getJSONObject("bounds")
                val northeastLat = bounds.getJSONObject("northeast").getDouble("lat")
                val northeastLng = bounds.getJSONObject("northeast").getDouble("lng")
                val southwestLat = bounds.getJSONObject("southwest").getDouble("lat")
                val southwestLng = bounds.getJSONObject("southwest").getDouble("lng")
                Log.d(
                    "maps-test",
                    "LatLngs: ${northeastLat}, ${northeastLng} | ${southwestLat}, ${southwestLng}"
                )
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds(
                            LatLng(southwestLat, southwestLng),
                            LatLng(northeastLat, northeastLng)
                        ), 100
                    )
                )
            },
            Response.ErrorListener { _ ->
            }) {}
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }

    private fun updateDriver(){
        car_location?.position = driver?.lat?.let { LatLng(it, driver!!.long) }
    }

    private fun setDriverArrivedOrCanceledListener(view: View){
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("activeRides").child(rideId.toString())
            .child("driver").child("status").addValueEventListener(object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val status = dataSnapshot.getValue<String>()
                    Log.d("Status change", status.toString())
                    if(status == "DriverCanceled"){
                        showRideCanceledNotification(view)
                    }
                    else if(status == "Completed"){
                        showRideCompletedNotification(view)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demoo", "cancel")
                }
            })
    }

    fun showRideCanceledNotification(view: View){
        val builder  =  AlertDialog.Builder(context);
        builder.setTitle("Ride Status");

        builder.setMessage("This ride was canceled by the driver")

        builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                dialog, id -> dialog.cancel()
        })

        builder.setCancelable(false)

        val dialog : AlertDialog = builder.create()
        dialog.show()


        val bundle = Bundle()
        bundle.putString("chatroomId", chatRoomId.toString())
        view.findNavController().navigate(R.id.action_nav_waiting_on_ride_to_chatroom, bundle)
    }

    fun showRideCompletedNotification(view: View){
        val builder  =  AlertDialog.Builder(context);
        builder.setTitle("Ride Status");

        builder.setMessage("The ride has been completed")

        builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                dialog, id -> dialog.cancel()
        })

        builder.setCancelable(false)

        val dialog : AlertDialog = builder.create()
        dialog.show()


        val bundle = Bundle()
        bundle.putString("chatroomId", chatRoomId.toString())
        view.findNavController().navigate(R.id.action_nav_waiting_on_ride_to_chatroom, bundle)
    }

    override fun onDestroy() {
        super.onDestroy()

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
            .child("driverRequests")
            .child(rideId!!).removeValue()

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
            .child("activeRides")
            .child(rideId!!).removeValue()
    }
}