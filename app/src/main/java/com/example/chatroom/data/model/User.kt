package com.example.chatroom.data.model

object User {
    var userId = ""
    var firstName = ""
    var lastName = ""
    var gender = ""
    var city = ""
    var email = ""
    var imageUrl = ""
    fun getValue(): Map<String, String> {
        val userValues = mapOf("userId" to userId, "firstName" to firstName, "lastName" to lastName, "gender" to gender, "city" to city, "email" to email, "imageUrl" to imageUrl)
        return userValues
    }
}