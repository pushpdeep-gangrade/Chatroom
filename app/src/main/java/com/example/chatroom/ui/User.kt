package com.example.chatroom.ui

import kotlinx.serialization.Serializable

@Serializable
data class User (val firstname: String, val lastname: String, val gender : String, val city : String, val profileImageUrl : String)

