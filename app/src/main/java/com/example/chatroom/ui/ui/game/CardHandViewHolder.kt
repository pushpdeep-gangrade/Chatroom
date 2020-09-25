package com.example.chatroom.ui.ui.game

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.example.chatroom.data.model.GameMaster
import com.example.chatroom.data.model.User
import com.example.chatroom.ui.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

import com.squareup.picasso.Picasso

class CardHandViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.card_hand_item, parent, false)) {

    private var mCardColorTextView : TextView? = null
    private var mCardValueTextView : TextView? = null

    init{
        mCardColorTextView = itemView.findViewById(R.id.card_hand_color_textView)
        mCardValueTextView = itemView.findViewById(R.id.card_hand_value_textView)
    }

    fun bind(cardValue: String, cardColor: String, gameRequestId: String, playerNum: Int, cardPosition: Int) {
        //default gray for +4
        var color = Color.GRAY
        when (cardColor) {
            "B" -> color = Color.BLUE
            "G" -> color = Color.GREEN
            "R" -> color = Color.RED
            "Y" -> color = Color.YELLOW
        }

        mCardValueTextView?.text = cardValue
        mCardColorTextView?.setBackgroundColor(color)

        mCardColorTextView?.setOnClickListener {
            Log.d("hand", "clicked ${cardColor}${cardValue}: playerNum: ${playerNum}")

            val gameMaster = GameRoomFragment.globalGameMaster
            var playerHand = GameRoomFragment.globalPlayerHand
            if (gameMaster != null) {
                if (gameMaster.playersTurn.last().toString() == playerNum.toString()) {
                    if (gameMaster.centerCard?.get(1) == cardValue[0] || gameMaster.centerCard?.get(0).toString() == cardColor) {
                        gameMaster.centerCard = cardColor.plus(cardValue)
                        MainActivity.dbRef.child("games").child("activeGames")
                            .child(gameRequestId).child("player${playerNum}hand")
                            .child(cardPosition.toString()).removeValue()

                        MainActivity.dbRef.child("games").child("activeGames")
                            .child(gameRequestId).child("player${playerNum}hand")
                            .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    playerHand?.clear()
                                    for (postSnapshot in snapshot.children) {
                                        var card1 = postSnapshot.getValue<String>()
                                        if (card1 != null) {
                                            playerHand?.add(card1)
                                        }
                                    }
                                    MainActivity.dbRef.child("games").child("activeGames")
                                        .child(gameRequestId).child("player${playerNum}hand")
                                        .setValue(playerHand)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("demo", "Cancelled")
                                }

                            })

                        // todo: add logic/ popup for invalid choices
                        if (gameMaster.centerCard?.get(1) != cardValue[0] && gameMaster.centerCard?.get(0) != cardColor[0]) {
                           Log.d("choice","Invalid choice")
                            /* Toast.makeText(
                                this,
                                "Invalid choice. Please choose another card",
                                Toast.LENGTH_LONG
                            ).show() */
                        }
                        if (playerNum == 1) {
                            gameMaster.playersTurn = "player2"
                        }
                        else if (playerNum == 2) {
                            gameMaster.playersTurn = "player1"
                        }

                        if (cardValue == "Skip") {
                            gameMaster.isSkipTurn = true
                        }
                        else if (cardValue == "+4") {
                            gameMaster.isDraw4Trun = true
                        }

                        MainActivity.dbRef.child("games").child("activeGames")
                            .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                    }
                }
            }

            //MainActivity.dbRef.child("games").child(gameRequestId).child("gameMaster").addListenerForSingleValueEvent(object :
            //ValueEventListener {
            //    override fun onDataChange(snapshot: DataSnapshot) {
            //        var gameMaster = snapshot.getValue<GameMaster>()
            //        Log.d("hand", "${gameMaster?.centerCard}")

            //        if (gameMaster != null) {
            //            if (gameMaster.playersTurn[-1].toString() == playerNum.toString()) {
            //                if (gameMaster.centerCard?.subSequence(1, -1) == cardValue || gameMaster.centerCard?.get(0).toString() == cardColor) {
            //                    gameMaster.centerCard = cardColor.plus(cardValue)
            //                    MainActivity.dbRef.child("games").child(gameRequestId).child("player${playerNum}hand").child(cardColor.plus(cardValue)).removeValue()

            //                    if (playerNum == 1) {
            //                        gameMaster.playersTurn = "player2"
            //                    }
            //                    else if (playerNum == 2) {
            //                        gameMaster.playersTurn = "player1"
            //                    }

            //                    if (cardValue == "Skip") {
            //                        gameMaster?.isSkipTurn = true
            //                    }
            //                    else if (cardValue == "+4") {
            //                        gameMaster?.isDraw4Trun = true
            //                    }

            //                    MainActivity.dbRef.child("games").child(gameRequestId).child("gameMaster").setValue(gameMaster)
            //                }
            //            }
            //        }
            //    }

            //    override fun onCancelled(error: DatabaseError) {
            //        Log.d("demo", "cancelled")
            //    }

            //})
        }
    }


}