package com.example.chatroom.ui.ui.rider

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.Places.createClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.chat_item.view.*
import java.util.*


class RequestRideFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentRequestRideBinding? = null
    private val binding get() = _binding!!
    lateinit var pickupLocationLatLng: LatLng
    lateinit var dropoffLocationLatLng: LatLng
    var pickupLocationPlace: PickedPlace = PickedPlace()
    var dropoffLocationPlace: PickedPlace = PickedPlace()
    var rider : User? = null

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
            var requestId: UUID = UUID.randomUUID()
            var rideRequest: RideRequest = RideRequest()

            rideRequest.pickupLocation = pickupLocationPlace
            rideRequest.dropoffLocation = dropoffLocationPlace
            rideRequest.requestId = requestId.toString()
            rideRequest.status = "Available"
            rideRequest.riderInfo = messageUser!!

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("rideRequests").child(requestId.toString()).setValue(rideRequest)

            val bundle = bundleOf("requestId" to chatRoomId, "requestId" to requestId.toString())
            view.findNavController().navigate(R.id.action_nav_request_ride_to_nav_request_driver, bundle)//, bundle)
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

                googleMap?.addMarker(MarkerOptions().position(pickupLocationLatLng)
                    .title(place.name))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(pickupLocationLatLng))
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

                googleMap?.addMarker(MarkerOptions().position(dropoffLocationLatLng)
                    .title(place.name))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(dropoffLocationLatLng))

                googleMap?.addPolygon(PolygonOptions().clickable(true).add(pickupLocationLatLng, dropoffLocationLatLng))
                Log.d("demo", "Place: ${place.latLng}, ${place.name}")
            }

            override fun onError(p0: Status) {
                Log.d("demo","error drop")
            }

        })

    }

    fun initialize(){
        val mapFragment : SupportMapFragment? = FragmentManager.findFragment(view?.findViewById(R.id.request_ride_mapView)!!)as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }




}
