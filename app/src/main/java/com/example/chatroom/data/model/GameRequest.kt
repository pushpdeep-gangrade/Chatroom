package com.example.chatroom.data.model

data class GameRequest(val gameRequestId: String = "", val player1: User = User(), var player1cards: MutableList<String>? = null, var player2: User? = User(), var player2cards: MutableList<String>? = null ) {
}