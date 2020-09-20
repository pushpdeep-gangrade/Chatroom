package com.example.chatroom.ui.ui.rider

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentRequestRideBinding
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


class RequestRideFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentRequestRideBinding? = null
    private val binding get() = _binding!!

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
            view.findNavController().navigate(R.id.action_nav_request_ride_to_nav_request_driver)//, bundle)
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {

        if (!Places.isInitialized()) {
            context?.let { Places.initialize(it, "AIzaSyBuIvBN797lPyHRIASQJzk77k0ry-UZTCI") };
        }
        val placesClient = context?.let { Places.createClient(it) }
        val pickUpAutoComplete = FragmentManager.findFragment(view?.findViewById(R.id.pickup_location_edittext)!!) as AutocompleteSupportFragment
        pickUpAutoComplete.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        pickUpAutoComplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val sydney = LatLng(-33.85, 151.211)
                googleMap?.addMarker(MarkerOptions().position(sydney)
                    .title("Sydney"))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                Log.d("demo", "Place: ${place.latLng}, ${place.name}")
            }

            override fun onError(p0: Status) {
                Log.d("demo","error pickup")
            }

        })


        val dropOffAutoComplete = FragmentManager.findFragment(view?.findViewById(R.id.dropoff_location_edittext)!!) as AutocompleteSupportFragment
        dropOffAutoComplete.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        dropOffAutoComplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                    val sydney = LatLng(-37.85, 144.211)
                googleMap?.addMarker(MarkerOptions().position(sydney)
                    .title("Melbourne"))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))

                googleMap?.addPolygon(PolygonOptions().clickable(true).add(LatLng(-33.85, 151.211),LatLng(-37.85, 144.211)))
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
