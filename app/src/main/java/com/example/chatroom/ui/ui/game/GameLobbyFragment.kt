package com.example.chatroom.ui.ui.game

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.data.model.*
import com.example.chatroom.databinding.FragmentGameLobbyBinding
import com.example.chatroom.ui.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.util.*
import kotlin.collections.ArrayList

class GameLobbyFragment : Fragment() {
    var currentUser: User? = null
    private var _binding: FragmentGameLobbyBinding? = null
    private val binding get() = _binding!!
    private var listActiveUsers: MutableList<User> = ArrayList()
    private var listGameRequests: MutableList<GameRequest> = ArrayList()
    private var listGameRequestsNames: MutableList<String> = ArrayList()
    private var currentGameRequest: GameRequest = GameRequest()
    var player2Name: String = "Player 2"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setGameRequestListener()

        MainActivity.dbRef.child("users").child(MainActivity.auth.currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d("demo", "Firebase event cancelled on getting user data")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    currentUser = dataSnapshot.getValue<User>()
                    Log.d("Current User", currentUser.toString())
                }
            })

        binding.gameLobbyGameRequestList.setOnItemClickListener() { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            showJoinDialog(listGameRequests[i], view)
        }

        binding.gameLobbyRequestGameButton.setOnClickListener {
            val gameRequestId: UUID = UUID.randomUUID()
            val gameRequest = GameRequest(
                gameRequestId.toString(),
                currentUser!!, null, null, null
            )

            currentGameRequest = gameRequest

            MainActivity.dbRef.child("games").child("gameRequests")
                .child(gameRequestId.toString()).setValue(gameRequest)


            val builder = AlertDialog.Builder(context)
            builder.setTitle("Game Request")

            builder.setMessage("Waiting for another player to join...")

            builder.setNegativeButton("Cancel", { dialog, id ->

                MainActivity.dbRef.child("games")
                    .child("gameRequests")
                    .child(gameRequest.gameRequestId).removeValue()

                dialog.cancel()
            })

            builder.setCancelable(false)

            val dialog: AlertDialog = builder.create()
            dialog.show()

            setPlayerTwoListener(gameRequest, view, dialog)


        }
    }

    private fun setPlayerTwoListener(gameRequest: GameRequest, view: View, dialog: AlertDialog) {
        MainActivity.dbRef.child("games").child("gameRequests")
            .child(gameRequest.gameRequestId).child("player2")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val u: User? = dataSnapshot.getValue<User>()

                    if (u != null) {

                        dialog.cancel()

                        showPlayerJoinedNotification(view)

                        MainActivity.dbRef.child("games")
                            .child("gameRequests")
                            .child(gameRequest.gameRequestId).removeValue()


                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demoo", "cancel")
                }
            })
    }

    private fun setGameRequestListener() {
        MainActivity.dbRef.child("games").child("gameRequests")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    listGameRequestsNames.clear()
                    listGameRequests.clear()

                    for (postSnapshot in dataSnapshot.children) {
                        val gr: GameRequest? = postSnapshot.getValue<GameRequest>()
                        Log.d("Player 2", gr?.player2.toString())
                        // player2Name = gr?.player2!!.firstName
                        if (gr != null) {
                            listGameRequests.add(gr)
                            listGameRequestsNames.add(
                                gr.player1.firstName.plus(" ").plus(gr.player1.lastName)
                            )
                            Log.d(
                                "Game Request Name",
                                gr.player1.firstName.plus(" ").plus(gr.player1.lastName)
                            )
                            if (player2Name.equals("Player 2") || player2Name.equals("")) {
                                player2Name = gr.player2!!.firstName
                            }
                            Log.d("players", "Player 2 is: " + player2Name + ".")
                        }
                    }

                    val arrayAdapter =
                        context?.let {
                            ArrayAdapter<String>(
                                it,
                                android.R.layout.simple_list_item_1,
                                listGameRequestsNames
                            )
                        }
                    binding.gameLobbyGameRequestList.adapter = arrayAdapter

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("demoo", "cancel")
                }
            })
    }

    private fun showJoinDialog(gameRequest: GameRequest, view: View) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Game Request")

        builder.setMessage("Join this game?")
        Log.d("Game Request ID", gameRequest.toString())

        builder.setPositiveButton("Join") { dialog, id ->

            Log.d("Game Request ID", gameRequest.gameRequestId)

            val changedGameRequest: GameRequest = gameRequest
            changedGameRequest.player2 = currentUser
            MainActivity.dbRef.child("games")
                .child("gameRequests").child(gameRequest.gameRequestId)
                .child("player2").setValue(currentUser)

            val cardList = mutableListOf<String>(
                "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BSkip", "BSkip",
                "G0", "G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "GSkip", "GSkip",
                "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "RSkip", "RSkip",
                "Y0", "Y1", "Y2", "Y3", "Y4", "Y5", "Y6", "Y7", "Y8", "Y9", "YSkip", "YSkip",
                "+4", "+4", "+4", "+4"
            )
            cardList.shuffle()
            val activeGame = ActiveGame(
                changedGameRequest.gameRequestId, changedGameRequest.player1, null,
                changedGameRequest.player2, null,
                GameMaster(true, true, "player1", null, cardList, false, false), null
            )

            MainActivity.dbRef.child("games")
                .child("activeGames")
                .child(gameRequest.gameRequestId)
                .setValue(activeGame)

            //MainActivity.dbRef.child("games")
            //    .child("activeGames")
            //    .child(gameRequest.gameRequestId).child("player1")
            //    .setValue(changedGameRequest.player1)

            //MainActivity.dbRef.child("games")
            //    .child("activeGames")
            //    .child(gameRequest.gameRequestId).child("player2")
            //    .setValue(changedGameRequest.player2)

            //MainActivity.dbRef.child("games")
            //    .child("activeGames")
            //    .child(gameRequest.gameRequestId).child("gameRequestId")
            //    .setValue(changedGameRequest.gameRequestId)

            val bundle = Bundle()
            bundle.putString("gameRequestId", changedGameRequest.gameRequestId)
            bundle.putInt("playerNumber", 2)
            view.findNavController().navigate(R.id.action_nav_game_lobby_to_nav_game_room, bundle)

        }

        builder.setNegativeButton("Close", { dialog, id ->
            dialog.cancel()
        })

        builder.setCancelable(false)

        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

    private fun showPlayerJoinedNotification(view: View) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Game Request")

        builder.setMessage("Another player has joined!")

        builder.setNegativeButton("Ok", { dialog, id ->
            dialog.cancel()
        })

        builder.setCancelable(false)

        val dialog: AlertDialog = builder.create()
        dialog.show()

        val bundle = Bundle()
        bundle.putString("gameRequestId", currentGameRequest.gameRequestId)
        bundle.putInt("playerNumber", 1)
        bundle.putString("p1_name", currentGameRequest.player1.firstName)
        bundle.putString("p2_name", player2Name)
        Log.d(
            "players",
            "Player 1: " + currentGameRequest.player1.firstName + " Player 2: " + player2Name
        )
        view.findNavController().navigate(R.id.action_nav_game_lobby_to_nav_game_room, bundle)
    }


}