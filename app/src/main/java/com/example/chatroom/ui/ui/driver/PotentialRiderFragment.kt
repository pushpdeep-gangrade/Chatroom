package com.example.chatroom.ui.ui.driver

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.PickedPlace
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.databinding.FragmentChatroomBinding
import com.example.chatroom.databinding.FragmentPotentialRiderBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.example.chatroom.ui.ui.chatroom.messageUser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.maps.android.PolyUtil
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.ArrayList

class PotentialRiderFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentPotentialRiderBinding? = null
    private val binding get() = _binding!!
    var requestId: String = ""
    var requestInfo : RideRequest? = null
    var pickupLocationPlace: PickedPlace = PickedPlace()
    var dropoffLocationPlace: PickedPlace = PickedPlace()
    var path: MutableList<List<LatLng>> = ArrayList()
    var urlDirections: String = "https://maps.googleapis.com/maps/api/directions/json?key=AIzaSyBuIvBN797lPyHRIASQJzk77k0ry-UZTCI"
    var directionsRequest: StringRequest? = null

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

                pickupLocationPlace = requestInfo?.pickupLocation!!
                dropoffLocationPlace = requestInfo?.dropoffLocation!!

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
        initialize()

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

    override fun onMapReady(googleMap: GoogleMap?) {
        directionsRequest = object : StringRequest(Request.Method.GET,
            urlDirections
                .plus("&origin=${pickupLocationPlace.latitude},${pickupLocationPlace.longitude}")
                .plus("&destination=${dropoffLocationPlace.latitude},${dropoffLocationPlace.longitude}"), Response.Listener<String> {
                    response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
                // Get bounds
                val bounds = routes.getJSONObject(0).getJSONObject("bounds")
                val northeastLat = bounds.getJSONObject("northeast").getDouble("lat")
                val northeastLng = bounds.getJSONObject("northeast").getDouble("lng")
                val southwestLat = bounds.getJSONObject("southwest").getDouble("lat")
                val southwestLng = bounds.getJSONObject("southwest").getDouble("lng")
                Log.d("maps-test", "LatLngs: ${northeastLat}, ${northeastLng} | ${southwestLat}, ${southwestLng}")
                googleMap?.addMarker(
                    MarkerOptions().position(LatLng(pickupLocationPlace.latitude, pickupLocationPlace.longitude)!!)
                    .title(pickupLocationPlace.name))
                googleMap?.addMarker(
                    MarkerOptions().position(LatLng(dropoffLocationPlace.latitude, dropoffLocationPlace.longitude)!!)
                        .title(dropoffLocationPlace.name))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(southwestLat, southwestLng), LatLng(northeastLat, northeastLng)), 100))
            }, Response.ErrorListener {
                    _ ->
            }){}
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)
    }

    fun initialize(){
        val mapFragment : SupportMapFragment? = FragmentManager.findFragment(view?.findViewById(R.id.potential_rider_mapView)!!)as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }
}