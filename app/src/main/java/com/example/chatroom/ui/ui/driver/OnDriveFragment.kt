package com.example.chatroom.ui.ui.driver

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.chatroom.R
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.databinding.FragmentOnDriveBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.chatRoomId
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

private var map: GoogleMap? = null
private var rideId: String? = null
private var rider: MapUser? = null
private var driver: MapUser? = null

class OnDriveFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentOnDriveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rideId = arguments?.getString("rideId")
        _binding = FragmentOnDriveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()

        binding.onDriveCancelButton.setOnClickListener {
            //    onDestroy()
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        getLocation()
    }

    fun initialize() {
        val mapFragment: SupportMapFragment? =
            FragmentManager.findFragment(view?.findViewById(R.id.onDrive_mapView)!!) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

    }

    fun getLocation() {
        if (rideId != null) {
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests")
                .child(rideId!!).child("drivers").addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (postSnapshot in dataSnapshot.children) {
                            driver = postSnapshot.getValue<MapUser>()
                            Log.d("driver", "rider name" + driver?.rider?.firstName.toString())
                            Log.d("drive", driver.toString())
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests")
                .child(rideId!!).child("rider").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        rider = dataSnapshot.getValue<MapUser>()
                        Log.d("demo", "rider name" + rider?.rider?.firstName.toString())
                        updateLocationUI()
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
        val riderLocation = rider?.lat?.let { rider?.long?.let { it1 -> LatLng(it, it1) } }
        val driverLocation = driver?.lat?.let { driver?.long?.let { it1 -> LatLng(it, it1) } }
        map?.addMarker(riderLocation?.let {
            MarkerOptions().position(it).title(rider?.rider?.firstName)
        })
        map?.addMarker(driverLocation?.let {
            MarkerOptions().position(it).title(driver?.rider?.firstName)
        })

        val polyline1 = map!!.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(
                    riderLocation, driverLocation
                )
        )

        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                riderLocation, 15F
            )
        )


    }

    override fun onDestroy() {
        super.onDestroy()
        findNavController().navigate(R.id.action_nav_on_drive_to_nav_chatrooms)
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
            .child("driverRequests")
            .child(rideId!!).removeValue()
    }


}