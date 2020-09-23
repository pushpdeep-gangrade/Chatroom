package com.example.chatroom.ui.ui.ridehistory

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.CompleteRide
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.databinding.FragmentRideDetailsBinding
import com.example.chatroom.databinding.FragmentRideHistoryBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.example.chatroom.ui.ui.driver.PotentialRiderFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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


class RideDetailsFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentRideDetailsBinding? = null
    private val binding get() = _binding!!
    var completeRide : CompleteRide? = null
    var requestId: String = ""
    private var map: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var urlDirections: String = "https://maps.googleapis.com/maps/api/directions/json?"
    var path: MutableList<List<LatLng>> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRideDetailsBinding.inflate(inflater, container, false)

        requestId = arguments?.getString("requestId").toString()

        MainActivity.dbRef.child("rideHistory").child(requestId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("demo", "Firebase event cancelled on getting user data")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                completeRide = dataSnapshot.getValue<CompleteRide>()

                var rider = completeRide?.rideRequest?.riderInfo
                var driver = completeRide?.driver?.driver

                Picasso.get().load(rider?.imageUrl).resize(250, 250).into(binding.rideDetailsRiderProfilePic)
                Picasso.get().load(driver?.imageUrl).resize(250, 250).into(binding.rideDetailsDriverProfilePic)

                //binding.rideDetailsRiderEmail.text = rider?.email
                binding.rideDetailsRiderFirst.text = rider?.firstName
                binding.rideDetailsRiderLast.text = rider?.lastName

                //binding.rideDetailsDriverEmail.text = driver?.email
                binding.rideDetailsDriverFirst.text = driver?.firstName
                binding.rideDetailsDriverLast.text = driver?.lastName

                binding.rideDetailsPickupLocation.text = completeRide?.rideRequest?.pickupLocation?.name
                binding.rideDetailsDropoffLocation.text = completeRide?.rideRequest?.dropoffLocation?.name

                binding.rideDetailsDriverDetailsContainer.setOnClickListener {
                    val bundle = bundleOf("userData" to driver?.userId)
                    view?.findNavController()?.navigate(R.id.action_nav_ride_details_to_nav_profile, bundle)
                }

                binding.rideDetailsRiderDetailsContainer.setOnClickListener {
                    val bundle = bundleOf("userData" to rider?.userId)
                    view?.findNavController()?.navigate(R.id.action_nav_ride_details_to_nav_profile, bundle)
                }

                setMap(completeRide)

            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
    }

    fun initialize(){
        val mapFragment : SupportMapFragment? = FragmentManager.findFragment(view?.findViewById(R.id.rideDetails_mapView)!!)as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        fusedLocationProviderClient =
            context?.let { LocationServices.getFusedLocationProviderClient(it) }!!
    }

    private fun setMap(completeRide: CompleteRide?){
        var rideRequest = completeRide?.rideRequest
        var directionsRequest: StringRequest? = null

        directionsRequest = object : StringRequest(Request.Method.GET,
            urlDirections
                .plus("key=${context?.resources?.getString(R.string.api_key)}")
                .plus("&origin=${rideRequest!!.pickupLocation.latitude},${rideRequest.pickupLocation!!.longitude}")
                .plus("&destination=${rideRequest!!.dropoffLocation.latitude},${rideRequest.dropoffLocation.longitude}"), Response.Listener<String> {
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
                    map!!.addPolyline(
                        PolylineOptions().addAll(path[i]).color(
                            Color.RED))
                }

                // Get bounds
                val bounds = routes.getJSONObject(0).getJSONObject("bounds")
                val northeastLat = bounds.getJSONObject("northeast").getDouble("lat")
                val northeastLng = bounds.getJSONObject("northeast").getDouble("lng")
                val southwestLat = bounds.getJSONObject("southwest").getDouble("lat")
                val southwestLng = bounds.getJSONObject("southwest").getDouble("lng")

                Log.d("maps-test", "LatLngs: ${northeastLat}, ${northeastLng} | ${southwestLat}, ${southwestLng}")

                map?.addMarker(
                    MarkerOptions().position(LatLng(rideRequest!!.pickupLocation.latitude, rideRequest!!.pickupLocation.longitude)!!)
                        .title(rideRequest!!.pickupLocation.name))
                map?.addMarker(
                    MarkerOptions().position(LatLng(rideRequest!!.dropoffLocation.latitude, rideRequest!!.dropoffLocation.longitude)!!)
                        .title(rideRequest!!.dropoffLocation.name))
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds(
                            LatLng(southwestLat, southwestLng), LatLng(northeastLat, northeastLng)
                        ), 100))
            }, Response.ErrorListener {
                    _ ->
            }){}
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)
    }


}