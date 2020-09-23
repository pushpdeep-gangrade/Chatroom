package com.example.chatroom.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MapUser(var driver : User = User(),
                var lat : Double = 0.0,
                var long : Double = 0.0,
                var status : String = "") {


    /*fun getValue(): Map<String, String> {
        val userValues = mapOf("userId" to userId, "firstName" to firstName, "lastName" to lastName, "gender" to gender, "city" to city, "email" to email, "imageUrl" to imageUrl)
        return userValues
    }*/
}