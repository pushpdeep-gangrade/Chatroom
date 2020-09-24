package com.example.chatroom.ui.ui.game

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentGameRoomBinding
import com.example.chatroom.ui.MainActivity


class GameRoomFragment : Fragment() {
    private var _binding: FragmentGameRoomBinding? = null
    private val binding get() = _binding!!
    private var gameRequestId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        gameRequestId = arguments?.getString("gameRequestId").toString()

        _binding = FragmentGameRoomBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
}