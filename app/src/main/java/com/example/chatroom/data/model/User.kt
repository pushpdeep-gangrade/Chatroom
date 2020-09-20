package com.example.chatroom.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(var userId: String = "",
                var firstName: String = "",
                var lastName: String = "",
                var gender: String = "",
                var city: String = "",
                var email: String = "",
                var imageUrl: String = "") {


    /*fun getValue(): Map<String, String> {
        val userValues = mapOf("userId" to userId, "firstName" to firstName, "lastName" to lastName, "gender" to gender, "city" to city, "email" to email, "imageUrl" to imageUrl)
        return userValues
    }*/
}