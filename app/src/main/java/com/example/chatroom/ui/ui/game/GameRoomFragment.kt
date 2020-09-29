package com.example.chatroom.ui.ui.game

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatroom.R
import com.example.chatroom.data.model.ActiveGame
import com.example.chatroom.data.model.GameMaster
import com.example.chatroom.databinding.FragmentGameRoomBinding
import com.example.chatroom.ui.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class GameRoomFragment : Fragment() {
    private var _binding: FragmentGameRoomBinding? = null
    private val binding get() = _binding!!
    private var gameRequestId: String = ""
    private var playerNum: Int? = null
    private var playerHand: MutableList<String> = mutableListOf<String>()
    private var gameMaster: GameMaster? = null
    private var dealCount: Int = 7
    private var previousCenterCard: String? = null
    private var centerCardColor: TextView? = null
    private var centerCardValue: TextView? = null
    private var playersTurnTextView: TextView? = null
    private var playersDB: DatabaseReference = MainActivity.db.getReference()
    private var player1Name: String? = "Player 1"
    private var player2Name: String? = "Player 2"
    private var tempCard: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        gameRequestId = arguments?.getString("gameRequestId").toString()
        Log.d("names", gameRequestId)
        playerNum = arguments?.getInt("playerNumber")

        _binding = FragmentGameRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        centerCardColor = binding.currentCardColorTextView
        centerCardValue = binding.currentCardValueTextView
        playersTurnTextView = binding.playersTurnTextView

        //region Winner/Exit Condition Check
        MainActivity.dbRef.child("games").child("activeGames")
            .child(gameRequestId).child("winner").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val winner = snapshot.getValue<String>()

                    if (winner != null) {
                        if (winner == "player1") {
                            Toast.makeText(context, "Host is the winner!", Toast.LENGTH_LONG).show()
                        } else if (winner == "player2") {
                            Toast.makeText(context, "Guest is the winner!", Toast.LENGTH_LONG)
                                .show()
                        }
                        MainActivity.dbRef.child("games")
                            .child("activeGames")
                            .child(gameRequestId).removeValue()
                        view.findNavController()
                            .navigate(R.id.action_nav_game_room_to_nav_game_lobby)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("demo", "Cancelled")
                }

            })
        //endregion Winner/Exit Condition Check

        //region PlayerHand Updates
        if (playerNum == 1) {
            MainActivity.dbRef.child("games").child("activeGames")
                .child(gameRequestId).child("player1hand")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        playerHand.clear()
                        for (postSnapshot in dataSnapshot.children) {
                            val card = postSnapshot.getValue<String>()
                            if (card != null) {
                                playerHand.add(card)
                            }
                        }
                        globalPlayerHand = playerHand
                        updateCards()
                        checkWinner()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("demo", "cancel")
                    }
                })
        } else if (playerNum == 2) {
            MainActivity.dbRef.child("games").child("activeGames")
                .child(gameRequestId).child("player2hand")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        playerHand.clear()
                        for (postSnapshot in dataSnapshot.children) {
                            val card = postSnapshot.getValue<String>()
                            if (card != null) {
                                playerHand.add(card)
                            }
                        }
                        globalPlayerHand = playerHand
                        updateCards()
                        checkWinner()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("demo", "cancel")
                    }
                })
        }
        //endregion PlayerHand Updates

        // Get player names
        playersDB.child("games").child("active games").child(gameRequestId).child("player1")
            .child("firstName")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    player1Name = snapshot.getValue<String>()
                    Log.d("names", "Player 1: " + player1Name)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("demo", "cancel")
                }
            })

        MainActivity.dbRef.child("games").child("active games").child(gameRequestId)
            .child("player2").child("firstName")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    player2Name = snapshot.getValue<String>()
                    Log.d("names", "Player 2: " + player2Name)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("demo", "cancel")
                }
            })


        //region GameMaster Control Code
        MainActivity.dbRef.child("games").child("activeGames")
            .child(gameRequestId).child("gameMaster").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    gameMaster = dataSnapshot.getValue<GameMaster>()
                    globalGameMaster = gameMaster
                    if (gameMaster != null && gameMaster?.isDealing != null && gameMaster?.gameIsActive != null) {
                        //region Turn Indicator
                        if (gameMaster?.isDealing!!) {
                            playersTurnTextView?.text = R.string.dealing.toString()
                        } else if (gameMaster?.playersTurn == "player1") {
                            if (player1Name != null) {
                                playersTurnTextView?.text = getString(R.string.turn,player1Name)
                            }
                            else {
                                playersTurnTextView?.text = R.string.p1turn.toString()
                            }
                        } else if (gameMaster?.playersTurn == "player2") {
                            if (player2Name != null) {
                                playersTurnTextView?.text = getString(R.string.turn,player2Name)
                            }
                            else {
                                playersTurnTextView?.text = R.string.p2turn.toString()
                            }                        }
                        //endregion Turn Indicator

                        //region Dealing Code
                        if (gameMaster?.centerCard == null) {
                            gameMaster?.centerCard = gameMaster!!.drawpile?.removeAt(0).toString()
                            previousCenterCard = gameMaster?.centerCard
                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                        } else if (gameMaster?.isDealing!! && gameMaster?.gameIsActive!!) {
                            //if dealing is finished
                            if (dealCount <= 0) {
                                gameMaster?.isDealing = false
                            } else {
                                if (playerNum == 1 && gameMaster?.playersTurn == "player1") {
                                    playerHand.add(gameMaster!!.drawpile?.removeAt(0).toString())
                                    gameMaster?.playersTurn = "player2"
                                    dealCount -= 1
                                } else if (playerNum == 2 && gameMaster?.playersTurn == "player2") {
                                    playerHand.add(gameMaster!!.drawpile?.removeAt(0).toString())
                                    gameMaster?.playersTurn = "player1"
                                    dealCount -= 1
                                }
                            }
                            if (playerNum == 1) {
                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player1hand").setValue(playerHand)
                            } else if (playerNum == 2) {
                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player2hand").setValue(playerHand)
                            }
                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                        }
                        //endregion Dealing Code

                        if (gameMaster?.playersTurn == "player${playerNum}") {
                            //region In-Game Code
                            if (!gameMaster?.isDealing!! && gameMaster?.gameIsActive!!) {
                                if (gameMaster?.isSkipTurn!!) {
                                    if (gameMaster?.playersTurn == "player1") {
                                        gameMaster?.playersTurn = "player2"
                                    } else if (gameMaster?.playersTurn == "player2") {
                                        gameMaster?.playersTurn = "player1"
                                    }
                                    gameMaster?.isSkipTurn = false
                                } else if (gameMaster?.isDraw4Turn!!) {
                                    for (x in 0 until 4) {
                                        playerHand.add(
                                            gameMaster!!.drawpile?.removeAt(0).toString()
                                        )
                                    }
                                    if (gameMaster?.playersTurn == "player1") {
                                        //uncomment if we want +4 to skip other player's turn
                                        //gameMaster?.playersTurn = "player2"
                                        MainActivity.dbRef.child("games").child("activeGames")
                                            .child(gameRequestId).child("player1hand")
                                            .setValue(playerHand)
                                    } else if (gameMaster?.playersTurn == "player2") {
                                        //uncomment if we want +4 to skip other player's turn
                                        //gameMaster?.playersTurn = "player1"
                                        MainActivity.dbRef.child("games").child("activeGames")
                                            .child(gameRequestId).child("player2hand")
                                            .setValue(playerHand)
                                    }

                                    gameMaster?.isDraw4Turn = false
                                }
                                //else regular turn

                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                            }
                            //endregion In-Game Code
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demo", "cancel")
                }
            })
        //endregion GameMaster Control Code

        //region Center Card Updates
        MainActivity.dbRef.child("games").child("activeGames")
            .child(gameRequestId).child("gameMaster").child("centerCard")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val centerCard = dataSnapshot.getValue<String>()
                    var color = Color.BLACK
                    when (centerCard?.get(0).toString()) {
                        "B" -> color = Color.parseColor("#1879A8")
                        "G" -> color = Color.parseColor("#5AB00D")
                        "R" -> color = Color.parseColor("#E63E27")
                        "Y" -> color = Color.parseColor("#F0DD1D")
                    }
                    centerCardColor!!.setBackgroundColor(color)

                    if (centerCard?.get(0).toString() == "+" || centerCard?.get(1)
                            .toString() == "+"
                    ) {
                        centerCardValue!!.text = "+4"
                    } else if (centerCard.toString().length > 3) {
                        centerCardValue!!.text = R.string.skip.toString()
                        centerCardValue!!.setTextSize(30F)
                    } else {
                        centerCardValue!!.setTextSize(60F)
                        centerCardValue!!.text = centerCard?.get(1).toString()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demo", "cancel")
                }
            })
        //endregion Center Card Updates

        //region Draw Card Button
        binding.drawCardButton.setOnClickListener {
            if (gameMaster != null) {
                if (!gameMaster?.isDealing!! && gameMaster?.gameIsActive!!) {
                    if (gameMaster?.playersTurn == "player${playerNum}" && !gameMaster?.isDraw4Turn!! && !gameMaster?.isSkipTurn!!) {

                        tempCard = gameMaster!!.drawpile?.removeAt(0).toString()

                        //region If Drawn Card is +4
                        //If drawn card is a +4
                        if (tempCard == "+4") {
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Choose Color")

                            //builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                            //        dialog, id -> dialog.cancel()
                            //})

                            val colorOptions =
                                mutableListOf("Blue", "Green", "Red", "Yellow").toTypedArray()
                            val colorValues = mutableListOf("B", "G", "R", "Y")

                            builder.setCancelable(false)

                            builder.setItems(
                                colorOptions,
                                { dialogInterface: DialogInterface, i: Int ->

                                    gameMaster?.centerCard = colorValues[i].plus("+4")

                                    if (playerNum == 1) {
                                        gameMaster?.playersTurn = "player2"
                                    } else if (playerNum == 2) {
                                        gameMaster?.playersTurn = "player1"
                                    }

                                    gameMaster?.isDraw4Turn = true

                                    MainActivity.dbRef.child("games").child("activeGames")
                                        .child(gameRequestId).child("gameMaster")
                                        .setValue(gameMaster)
                                })

                            val dialog: AlertDialog = builder.create()
                            dialog.show()
                        }
                        //endregion If Drawn Card is +4
                        //region If Drawn Card Matches Center (Color/Value)
                        //If drawn card color matches center card OR if drawn card value matches center card
                        else if (tempCard!![0] == gameMaster?.centerCard!![0] || tempCard!![1] == gameMaster?.centerCard!![1]) {
                            gameMaster?.centerCard = tempCard

                            if (playerNum == 1) {
                                gameMaster?.playersTurn = "player2"
                            } else if (playerNum == 2) {
                                gameMaster?.playersTurn = "player1"
                            }

                            if (tempCard!!.length > 3) {
                                gameMaster?.isSkipTurn = true
                            }

                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                        }
                        //endregion If Drawn Card Matches Center (Color/Value)
                        //region Unplayable Card Was Drawn
                        else {
                            playerHand.add(tempCard!!)
                            //tempCard = gameMaster!!.drawpile?.removeAt(0).toString()

                            if (playerNum == 1) {
                                //gameMaster?.playersTurn = "player2"
                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player1hand").setValue(playerHand)
                            } else if (playerNum == 2) {
                                //gameMaster?.playersTurn = "player1"
                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player2hand").setValue(playerHand)
                            }

                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                        }
                        //endregion Unplayable Card Was Drawn
                    }
                }
            }
        }
    }

    fun updateCards() {
        binding.cardHandRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.cardHandRecyclerView.adapter =
            CardHandAdapter(playerHand, gameRequestId, playerNum!!)
    }

    fun checkWinner() {
        if (gameMaster != null) {
            if (!gameMaster?.isDealing!! && playerHand.size == 1) {
                Toast.makeText(context, "UNO!", Toast.LENGTH_LONG).show()
            }
            if (!gameMaster?.isDealing!! && playerHand.size == 0) {
                MainActivity.dbRef.child("games").child("activeGames")
                    .child(gameRequestId).child("winner").setValue("player${playerNum}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        MainActivity.dbRef.child("games")
            .child("activeGames")
            .child(gameRequestId).removeValue()

        /* val builder  =  AlertDialog.Builder(context);
         builder.setTitle("Game Room");

         builder.setMessage("Player has left the game")

         builder.setNegativeButton("Ok", DialogInterface.OnClickListener {
                 dialog, id -> dialog.cancel()
         })

         builder.setCancelable(false)

         val dialog : AlertDialog = builder.create()
         dialog.show()

         view?.findNavController()?.navigate(R.id.action_nav_game_room_to_nav_game_lobby)*/
    }

    companion object {
        var globalGameMaster: GameMaster? = null
        var globalPlayerHand: MutableList<String>? = null
    }
}