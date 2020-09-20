package com.example.chatroom.ui.ui.rider

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.PickedPlace
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentRequestRideBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.example.chatroom.ui.ui.chatroom.messageUser
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.Places.createClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.chat_item.view.*
import kotlinx.android.synthetic.main.fragment_request_ride.*
import org.json.JSONObject
import java.util.*


class RequestRideFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentRequestRideBinding? = null
    private val binding get() = _binding!!
    var pickupLocationLatLng: LatLng? = null
    var dropoffLocationLatLng: LatLng? = null
    var pickupLocationPlace: PickedPlace = PickedPlace()
    var dropoffLocationPlace: PickedPlace = PickedPlace()
    var rider : User? = null
    var path: MutableList<List<LatLng>> = ArrayList()
    var urlDirections: String = "https://maps.googleapis.com/maps/api/directions/json?key=AIzaSyBuIvBN797lPyHRIASQJzk77k0ry-UZTCI"
    var directionsRequest: StringRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()

        binding.submitRequestButton.setOnClickListener {
            if (pickupLocationPlace.name != "" && dropoffLocationPlace.name != "") {
                var requestId: UUID = UUID.randomUUID()
                var rideRequest: RideRequest = RideRequest()

                rideRequest.pickupLocation = pickupLocationPlace
                rideRequest.dropoffLocation = dropoffLocationPlace
                rideRequest.requestId = requestId.toString()
                rideRequest.status = "Available"
                rideRequest.riderInfo = messageUser!!

                MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                    .child("rideRequests").child(requestId.toString()).setValue(rideRequest)

                val bundle =
                    bundleOf("chartroomId" to chatRoomId, "requestId" to requestId.toString())
                view.findNavController()
                    .navigate(R.id.action_nav_request_ride_to_nav_request_driver, bundle)//, bundle)
            }
            else {
                Toast.makeText(context, "Enter a pickup and dropoff location", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (!Places.isInitialized()) {
            context?.let { Places.initialize(it, "AIzaSyBuIvBN797lPyHRIASQJzk77k0ry-UZTCI") };
        }
        val placesClient = context?.let { Places.createClient(it) }
        val pickUpAutoComplete = FragmentManager.findFragment(view?.findViewById(R.id.pickup_location_edittext)!!) as AutocompleteSupportFragment
        pickUpAutoComplete.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        pickUpAutoComplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                var lat : Double = 0.0
                var long : Double = 0.0

                if(place != null){
                    lat = place.getLatLng()!!.latitude.toDouble()
                    long = place.getLatLng()!!.longitude.toDouble()
                }

                pickupLocationLatLng = LatLng(lat, long)

                pickupLocationPlace.id = place.id.toString()
                pickupLocationPlace.latitude = lat
                pickupLocationPlace.longitude = long
                pickupLocationPlace.name = place.name.toString()

                googleMap?.addMarker(MarkerOptions().position(pickupLocationLatLng!!)
                    .title(place.name))

                if (pickupLocationLatLng != null && dropoffLocationLatLng != null) {
                    directionsRequest = object : StringRequest(Request.Method.GET,
                        urlDirections
                            .plus("&origin=${pickupLocationLatLng?.latitude},${pickupLocationLatLng?.longitude}")
                            .plus("&destination=${dropoffLocationLatLng?.latitude},${dropoffLocationLatLng?.longitude}"), Response.Listener<String> {
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
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(southwestLat, southwestLng), LatLng(northeastLat, northeastLng)), 100))
                        }, Response.ErrorListener {
                                _ ->
                        }){}
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(directionsRequest)
                }
                else {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLng(pickupLocationLatLng))
                    googleMap?.moveCamera(CameraUpdateFactory.zoomTo(5.0f))
                }

                Log.d("demo", "Place: ${place.latLng}, ${place.name}")
            }

            override fun onError(p0: Status) {
                Log.d("demo","error pickup")
            }

        })


        val dropOffAutoComplete = FragmentManager.findFragment(view?.findViewById(R.id.dropoff_location_edittext)!!) as AutocompleteSupportFragment
        dropOffAutoComplete.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        dropOffAutoComplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                var lat : Double = 0.0
                var long : Double = 0.0

                if(place != null){
                    lat = place.getLatLng()!!.latitude.toDouble()
                    long = place.getLatLng()!!.longitude.toDouble()
                }

                dropoffLocationLatLng = LatLng(lat, long)

                dropoffLocationPlace.id = place.id.toString()
                dropoffLocationPlace.latitude = lat
                dropoffLocationPlace.longitude = long
                dropoffLocationPlace.name = place.name.toString()

                googleMap?.addMarker(MarkerOptions().position(dropoffLocationLatLng!!)
                    .title(place.name))

                if (pickupLocationLatLng != null && dropoffLocationLatLng != null) {
                    directionsRequest = object : StringRequest(Request.Method.GET,
                        urlDirections
                            .plus("&origin=${pickupLocationLatLng?.latitude},${pickupLocationLatLng?.longitude}")
                            .plus("&destination=${dropoffLocationLatLng?.latitude},${dropoffLocationLatLng?.longitude}"), Response.Listener<String> {
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
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(southwestLat, southwestLng), LatLng(northeastLat, northeastLng)), 100))
                        }, Response.ErrorListener {
                                _ ->
                        }){}
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(directionsRequest)
                }
                else {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLng(dropoffLocationLatLng))
                    googleMap?.moveCamera(CameraUpdateFactory.zoomTo(5.0f))
                }

                Log.d("demo", "Place: ${place.latLng}, ${place.name}")
            }

            override fun onError(p0: Status) {
                Log.d("demo","error drop")
            }

        })

        binding.requestRideClearMarkersButton.setOnClickListener {
            googleMap?.clear()
            pickUpAutoComplete.setText("")
            dropOffAutoComplete.setText("")
            pickupLocationPlace = PickedPlace()
            pickupLocationLatLng = null
            dropoffLocationPlace = PickedPlace()
            dropoffLocationLatLng = null
            path = ArrayList()
        }
    }

    fun initialize(){
        val mapFragment : SupportMapFragment? = FragmentManager.findFragment(view?.findViewById(R.id.request_ride_mapView)!!)as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }




}
