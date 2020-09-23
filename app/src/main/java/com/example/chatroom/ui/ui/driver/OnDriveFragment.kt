package com.example.chatroom.ui.ui.driver

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chatroom.R
import com.example.chatroom.data.model.CompleteRide
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.databinding.FragmentOnDriveBinding
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
import java.util.*
import kotlin.concurrent.schedule


private var map: GoogleMap? = null
private var rideId: String? = null
private var rider: MapUser? = null
private var driver: MapUser? = null
private var ride: RideRequest? = null
private var dropOff : LatLng? =null
private var directionsRequest: StringRequest? = null
private var urlDirections: String = "https://maps.googleapis.com/maps/api/directions/json?"
private var path: MutableList<List<LatLng>> = ArrayList()



class OnDriveFragment : Fragment(), OnMapReadyCallback {
    private var location : LocationManager ?=null
    private var _binding: FragmentOnDriveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rideId = arguments?.getString("rideId")
        ride = arguments?.getSerializable("ride") as RideRequest?
        Log.d("Ride", ride.toString())
        _binding = FragmentOnDriveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()

        binding.onDriveCancelButton.setOnClickListener {
            //    onDestroy()
            val bundle = Bundle()
            bundle.putString("chatroomId", chatRoomId.toString())

            findNavController().navigate(R.id.action_nav_on_drive_to_chatroom, bundle)
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("driverRequests")
                .child(rideId!!).removeValue()

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("activeRides")
                .child(rideId!!).removeValue()
        }

        binding.onDriveDoneButton.setOnClickListener {
            //    ride completed
            findNavController().navigate(R.id.action_nav_on_drive_to_nav_chatrooms)

            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                .child("activeRides").child(rideId!!.toString()).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("demo", "Firebase event cancelled on getting user data")
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val completeRide: CompleteRide? = dataSnapshot.getValue<CompleteRide>()

                        Log.d("On Drive Ride", completeRide.toString())
                        Log.d("On Drive Driver", driver.toString())

                        //val completeRide: CompleteRide = CompleteRide(ride!!, driver!!)

                        MainActivity.dbRef.child("rideHistory").child(rideId.toString()).setValue(completeRide)

                        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                            .child("activeRides")
                            .child(rideId!!).removeValue()
                    }
                })

        }

        location = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (context?.let {
                ActivityCompat.checkSelfPermission( it,Manifest.permission.ACCESS_FINE_LOCATION)
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(it,Manifest.permission.ACCESS_COARSE_LOCATION)
            } != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        location?.requestLocationUpdates( LocationManager.GPS_PROVIDER,  2000L,  10f, locationListenerGPS)


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
                            if(driver?.status.equals("Accepted"))
                                Log.d("driver", "driver name" + driver?.driver?.firstName.toString())
                            Log.d("drive", driver.toString())
                        }
                        Timer("SettingUp", false).schedule(1000) {
                            rideListner()
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })



        }
    }

   fun rideListner(){
       MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
           .child("driverRequests")
           .child(rideId!!).child("ride").addValueEventListener(object : ValueEventListener {
               override fun onDataChange(dataSnapshot: DataSnapshot) {
                   Log.d("demo", dataSnapshot.toString())
                   val curent_ride = dataSnapshot.getValue<RideRequest>()
                   Log.d("demo", " current ride " + curent_ride?.pickupLocation?.latitude.toString())

                   if (curent_ride != null) {
                       rider = MapUser(curent_ride.riderInfo, curent_ride.pickupLocation.latitude, curent_ride.pickupLocation.longitude, curent_ride.status)
                       dropOff = LatLng(curent_ride?.dropoffLocation.latitude, curent_ride?.dropoffLocation.longitude)
                       updateLocationUI()
                   }

                   Log.d("demo", rider?.lat.toString() + " rider location " + rider?.long.toString())
                   Log.d("demo", dropOff.toString())
               }

               override fun onCancelled(databaseError: DatabaseError) {
               }
           })
   }
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        map?.clear()
        var riderLocation = LatLng(0.0,0.0)
        var driverLocation = LatLng(0.0, 0.0)
        if(rider!=null && driver!=null){
         riderLocation = rider?.lat?.let { rider?.long?.let { it1 -> LatLng(it, it1) } }!!
         driverLocation = driver?.lat?.let { driver?.long?.let { it1 -> LatLng(it, it1) } }!!
        }


        var a = Location("rider")
        var b = Location("drive")

        a.latitude = riderLocation.latitude
        a.longitude = riderLocation.longitude

        b.latitude =driverLocation.latitude
        b.longitude= driverLocation.longitude

        if(a.distanceTo(b) <= 200)
            Toast.makeText(context, "Rider picked up", Toast.LENGTH_LONG).show()

        map?.addMarker(riderLocation?.let {
            MarkerOptions().position(it).title(rider?.driver?.firstName)
        })

        map?.addMarker(driverLocation?.let {
            dropOff?.let { it1 -> MarkerOptions().position(it1).title("Dropoff") }
        })

        map?.addMarker(driverLocation?.let {
            MarkerOptions().position(it).title(driver?.driver?.firstName).icon(BitmapDescriptorFactory.fromResource(R.drawable.driver_icon))
        })

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
                        map!!.addPolyline(
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



    override fun onDestroy() {
        super.onDestroy()
        findNavController().navigate(R.id.action_nav_on_drive_to_nav_chatrooms)
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
            .child("driverRequests")
            .child(rideId!!).removeValue()

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
            .child("activeRides")
            .child(rideId!!).removeValue()
    }

    private fun isLocationEnabled() {
        if (!location?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
            alertDialog.setTitle("Enable Location")
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.")
            alertDialog.setPositiveButton(
                "Location Settings",
                DialogInterface.OnClickListener { dialog, which ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                })
            alertDialog.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            val alert: AlertDialog = alertDialog.create()
            alert.show()
        }
    }

    var locationListenerGPS: LocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude


                driver?.driver?.userId?.let {
                    MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                        .child("driverRequests")
                        .child(rideId!!).child("drivers").child(it).child("lat").setValue(latitude)
                }

                    driver?.driver?.userId?.let {
                        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString())
                            .child("driverRequests")
                            .child(rideId!!).child("drivers").child(it).child("long").setValue(longitude)

                    }

            }

            override fun onStatusChanged(
                provider: String,
                status: Int,
                extras: Bundle
            ) {
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }


    override fun onResume() {
        super.onResume()
        isLocationEnabled()
    }


}