package com.example.chatroom.data.model

data class ActiveGame(val gameRequestId: String = "",
                      val player1: User = User(),
                      var player1hand: MutableList<String>? = null,
                      var player2: User? = User(),
                      var player2hand: MutableList<String>? = null,
                      val gameMaster: GameMaster? = GameMaster() ) {
}