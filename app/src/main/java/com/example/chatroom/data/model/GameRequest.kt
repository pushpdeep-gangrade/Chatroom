package com.example.chatroom.data.model

data class GameRequest(val gameRequestId: String = "", val player1: User = User(), var player2: User? = User() ) {
}