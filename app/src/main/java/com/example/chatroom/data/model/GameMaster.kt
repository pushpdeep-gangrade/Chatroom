package com.example.chatroom.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameMaster(var isDealing: Boolean = true,
                      var gameIsActive: Boolean = false,
                      var playersTurn: String = "player1",
                      var centerCard: String? = null,
                      var drawpile: MutableList<String>? = null,
                      var isDraw4Turn: Boolean = false,
                      var isSkipTurn: Boolean = false)