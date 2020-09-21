package com.example.chatroom.ui.ui.driver

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.Places
class UserCurrentLocation {


    private lateinit var placesClient: PlacesClient


    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


//    Places.initialize(applicationContext, getString(R.string.maps_api_key))
//    placesClient = Places.createClient(this)
//
//    // Construct a FusedLocationProviderClient.
//    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


   // mGeoDataClient = Places.getGeoDataClient(this, null);

    // Construct a PlaceDetectionClient.
  //  mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

    // Construct a FusedLocationProviderClient.
  //  mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

}