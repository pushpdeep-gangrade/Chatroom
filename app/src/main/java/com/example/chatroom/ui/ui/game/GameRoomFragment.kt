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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        gameRequestId = arguments?.getString("gameRequestId").toString()
        playerNum = arguments?.getInt("playerNumber")

        _binding = FragmentGameRoomBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        centerCardColor = binding.currentCardColorTextView
        centerCardValue = binding.currentCardValueTextView
        playersTurnTextView = binding.playersTurnTextView

        if (playerNum == 1) {
            MainActivity.dbRef.child("games").child("activeGames")
                .child(gameRequestId).child("player1hand").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        playerHand.clear()
                        for (postSnapshot in dataSnapshot.children) {
                            var card = postSnapshot.getValue<String>()
                            if (card != null) {
                                playerHand.add(card)
                            }
                        }
                        updateCards()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("demo", "cancel")
                    }
                })
        }
        else if (playerNum == 2) {
            MainActivity.dbRef.child("games").child("activeGames")
                .child(gameRequestId).child("player2hand").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        playerHand.clear()
                        for (postSnapshot in dataSnapshot.children) {
                            var card = postSnapshot.getValue<String>()
                            if (card != null) {
                                playerHand.add(card)
                            }
                        }
                        updateCards()
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("demo", "cancel")
                    }
                })
        }

        MainActivity.dbRef.child("games").child("activeGames")
            .child(gameRequestId).child("gameMaster").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    gameMaster = dataSnapshot.getValue<GameMaster>()
                    globalGameMaster = gameMaster

                    if (gameMaster?.playersTurn == "player1") {
                        playersTurnTextView?.text = "Host's Turn"
                    }
                    else if (gameMaster?.playersTurn == "player2") {
                        playersTurnTextView?.text = "Guest's Turn"
                    }

                    if (gameMaster?.isDealing != null && gameMaster?.gameIsActive != null) {
                        if (gameMaster?.centerCard == null) {
                            gameMaster?.centerCard = gameMaster!!.drawpile?.removeAt(0).toString()
                            previousCenterCard = gameMaster?.centerCard
                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                        }
                        else if (gameMaster?.isDealing!! && gameMaster?.gameIsActive!!) {
                            //if dealing is finished
                            if (dealCount <= 0) {
                                gameMaster?.isDealing = false
                            }
                            else {
                                if (playerNum == 1 && gameMaster?.playersTurn == "player1") {
                                    playerHand.add(gameMaster!!.drawpile?.removeAt(0).toString())
                                    gameMaster?.playersTurn = "player2"
                                    dealCount -= 1
                                }
                                else if (playerNum == 2 && gameMaster?.playersTurn == "player2") {
                                    playerHand.add(gameMaster!!.drawpile?.removeAt(0).toString())
                                    gameMaster?.playersTurn = "player1"
                                    dealCount -= 1
                                }
                            }
                            if (playerNum == 1) {
                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player1hand").setValue(playerHand)
                            }
                            else if (playerNum == 2) {
                                MainActivity.dbRef.child("games").child("activeGames")
                                    .child(gameRequestId).child("player2hand").setValue(playerHand)
                            }
                            MainActivity.dbRef.child("games").child("activeGames")
                                .child(gameRequestId).child("gameMaster").setValue(gameMaster)
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demo", "cancel")
                }
            })

        MainActivity.dbRef.child("games").child("activeGames")
            .child(gameRequestId).child("gameMaster").child("centerCard").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var centerCard = dataSnapshot.getValue<String>()

                    var color = Color.GRAY
                    when (centerCard?.get(0).toString()) {
                        "B" -> color = Color.BLUE
                        "G" -> color = Color.GREEN
                        "R" -> color = Color.RED
                        "Y" -> color = Color.YELLOW
                    }

                    centerCardColor!!.setBackgroundColor(color)

                    if (centerCard?.get(0).toString() == "+") {
                        centerCardValue!!.text = centerCard.toString()
                    }
                    else if (centerCard.toString().length > 2) {
                        centerCardValue!!.text = "Skip"
                    }
                    else {
                        centerCardValue!!.text = centerCard?.get(1).toString()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demo", "cancel")
                }
            })
    }

    fun updateCards() {
        binding.cardHandRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.cardHandRecyclerView.adapter = CardHandAdapter(playerHand, gameRequestId, playerNum!!)
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
    }
}