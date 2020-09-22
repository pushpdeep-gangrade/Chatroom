package com.example.chatroom.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CompleteRide(val rideRequest: RideRequest = RideRequest(),
                        val driver: MapUser = MapUser()
) {
}