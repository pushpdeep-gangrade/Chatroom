package com.example.chatroom.ui.ui.chatroom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.data.model.PickedPlace
import com.example.chatroom.databinding.FragmentPotentialRiderBinding
import com.example.chatroom.databinding.FragmentSharedLocationBinding
import com.example.chatroom.ui.ui.driver.PotentialRiderFragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class SharedLocationFragment : Fragment(), OnMapReadyCallback {
    private var _binding : FragmentSharedLocationBinding? = null
    private val binding get() = _binding!!
    var userName: String = ""
    var locationLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        userName = arguments?.getString("sharedLocationUserName").toString()
        locationLatLng = LatLng(arguments?.getDouble("sharedLocationLat")!!, arguments?.getDouble("sharedLocationLng")!!)

        _binding = FragmentSharedLocationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var nameTextView = binding.sharedLocationUserNameTextview
        nameTextView.text = userName

        binding.sharedLocationBackToChatroomButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("chatroomId", chatRoomId.toString())
            view.findNavController().navigate(R.id.action_nav_shared_location_to_chatroom, bundle)
        }
        initialize()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.addMarker(MarkerOptions().title(userName).position(locationLatLng!!))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 15.0f))
    }

    fun initialize(){
        val mapFragment : SupportMapFragment? = FragmentManager.findFragment(view?.findViewById(R.id.shared_location_mapView)!!)as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }
}