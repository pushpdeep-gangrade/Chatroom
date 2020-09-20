package com.example.chatroom.data.model

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class PickedPlace (var id: String = "", var latitude: Double = 0.0, var longitude: Double = 0.0, var name: String = ""){
    constructor() : this("", 0.0, 0.0, "")

}