package com.example.chatroom.ui.ui.game

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
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
import kotlin.coroutines.coroutineContext

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
        var color = Color.BLACK
        when (cardColor) {
            "B" -> color = Color.parseColor("#1879A8")
            "G" -> color = Color.parseColor("#5AB00D")
            "R" -> color = Color.parseColor("#E63E27")
            "Y" -> color = Color.parseColor("#F0DD1D")
        }

        mCardValueTextView?.text = cardValue
        if (mCardValueTextView?.text?.equals("Skip")!!) {
            mCardValueTextView!!.setTextSize(30F)
        }
        else {
            mCardValueTextView!!.setTextSize(60F)
        }
        mCardColorTextView?.setBackgroundColor(color)

        mCardColorTextView?.setOnClickListener {
            Log.d("hand", "clicked ${cardColor}${cardValue}: playerNum: ${playerNum}")

            val gameMaster = GameRoomFragment.globalGameMaster
            val playerHand = GameRoomFragment.globalPlayerHand
            if (gameMaster != null) {
                if (!gameMaster.isDealing && !gameMaster.isDraw4Turn && !gameMaster.isSkipTurn) {
                    if (gameMaster.playersTurn.last().toString() == playerNum.toString()) {
                        //region +4 Logic
                        if (cardValue == "+4") {
                            val builder  =  AlertDialog.Builder(this.itemView.context)
                            builder.setTitle("Choose Color")

                            //builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                            //        dialog, id -> dialog.cancel()
                            //})

                            val colorOptions = mutableListOf("Blue", "Green", "Red", "Yellow").toTypedArray()
                            val colorValues = mutableListOf("B", "G", "R", "Y")

                            builder.setCancelable(false)

                            builder.setItems(colorOptions, { dialogInterface: DialogInterface, i: Int ->

                                gameMaster.centerCard = colorValues[i].plus(cardValue)

                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player${playerNum}hand")
                                    .child(cardPosition.toString()).removeValue()

                                if(gameMaster.discardPile == null){
                                    gameMaster.discardPile = ArrayList()
                                }

                                gameMaster.discardPile?.add(cardValue)

                                Log.d("Discard Pile", gameMaster.discardPile.toString())

                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("gameMaster").setValue(gameMaster)


                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player${playerNum}hand")
                                    .addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            playerHand?.clear()
                                            for (postSnapshot in snapshot.children) {
                                                val card1 = postSnapshot.getValue<String>()
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

                                if (playerNum == 1) {
                                    gameMaster.playersTurn = "player2"
                                } else if (playerNum == 2) {
                                    gameMaster.playersTurn = "player1"
                                }

                                if (cardValue == "+4") {
                                    gameMaster.isDraw4Turn = true
                                }

                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                            })

                            val dialog : AlertDialog = builder.create()
                            dialog.show()
                        }
                        //endregion +4 Logic
                        //region Matching Card Value/Color Logic OR If Game Starts with +4 (if it is a played +4 it will have a color [i.e. G+4, R+4, etc])
                        else if (gameMaster.centerCard?.get(1) == cardValue[0] || gameMaster.centerCard?.get(0).toString() == cardColor || gameMaster.centerCard == "+4") {

                            gameMaster.centerCard = cardColor.plus(cardValue)

                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("player${playerNum}hand")
                                .child(cardPosition.toString()).removeValue()

                            if(gameMaster.discardPile == null){
                                gameMaster.discardPile = ArrayList()
                            }

                            gameMaster.discardPile?.add(cardColor.plus(cardValue))

                            Log.d("Discard Pile", gameMaster.discardPile.toString())

                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)


                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("player${playerNum}hand")
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        playerHand?.clear()
                                        for (postSnapshot in snapshot.children) {
                                            val card1 = postSnapshot.getValue<String>()
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

                            if (playerNum == 1) {
                                gameMaster.playersTurn = "player2"
                            } else if (playerNum == 2) {
                                gameMaster.playersTurn = "player1"
                            }

                            if (cardValue == "Skip") {
                                gameMaster.isSkipTurn = true
                            }

                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                        }
                        //endregion Matching Card Value/Color Logic OR If Game Starts with +4 (if it is a played +4 it will have a color [i.e. G+4, R+4, etc])
                        else {
                            // todo: add logic/ popup for invalid choices
                            Log.d(
                                "choice",
                                "Try again: Center card was " + gameMaster.centerCard?.get(1) + gameMaster.centerCard?.get(
                                    0
                                ) + " You played: " + cardValue[0] + cardColor[0]
                            )
                            /*
                            Toast.makeText(
                                    this,
                                    "Invalid choice. Please choose another card",
                                    Toast.LENGTH_LONG
                                ).show()
                         */
                        }
                    } else {
                        // todo: add logic/ popup for not player's turn
                        Log.d(
                            "choice",
                            "Please wait: you are player${playerNum} but it is ${gameMaster.playersTurn}'s turn"
                        )
                        /*  val builder  =  AlertDialog.Builder();
                        builder.setTitle("Invalid Choice");
                        builder.setMessage("Use another card or draw.")
                        builder.setNegativeButton("Ok", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                        val dialog : AlertDialog = builder.create()
                        dialog.show()

                        Toast.makeText(
                                    this,
                                    "Invalid choice. Please choose another card",
                                    Toast.LENGTH_LONG
                                ).show()
                        */

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
            //                        gameMaster?.isDraw4Turn = true
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