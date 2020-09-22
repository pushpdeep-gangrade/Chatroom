package com.example.chatroom.data.model

import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class RideRequest(var requestId : String = "",
                       var pickupLocation: PickedPlace = PickedPlace(),
                       var dropoffLocation: PickedPlace = PickedPlace(),
                       var riderInfo: User = User(),
                       var status : String = "") : java.io.Serializable {

}