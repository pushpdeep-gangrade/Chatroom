package com.example.chatroom.ui.ui.chatroom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentCreateChatroomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateChatroomFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding : FragmentCreateChatroomBinding? = null
    private val binding get() = _binding!!
    private var auth = FirebaseAuth.getInstance()
    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_create_chatroom, container, false)
        _binding = FragmentCreateChatroomBinding.inflate(inflater, container, false)
        dbRef = db.reference

        val chatroom_name_edittext = binding.root.findViewById<EditText>(R.id.create_chatroom_text_box)
        var fbUser = auth.currentUser
        var fbUserId = fbUser?.uid

        binding.root.findViewById<TextView>(R.id.create_chatroom_action).setOnClickListener {
            var chatroom_name_text = chatroom_name_edittext.text.toString()
            var allValid = true

            if(chatroom_name_text.equals("")){
                chatroom_name_edittext.error = "Please enter a chatroom name"
                allValid = false
            }

            if (fbUserId == null) {
                Log.d("demo", "(CreateChatroomFragment.kt) There is a user issue, user id: ${fbUserId}")
                allValid = false
            }

            if (allValid) {
                storeChatroomData(chatroom_name_text, fbUser)
            }
        }

        binding.root.findViewById<TextView>(R.id.cancel_create_chatroom_action).setOnClickListener {
            binding.cancelCreateChatroomAction.findNavController().navigate(R.id.action_nav_create_chatroom_to_nav_chatrooms)
        }

        return binding.root
    }

    private fun storeChatroomData(chatroom_name: String?, fbUser: FirebaseUser?){
        var fbUserId = fbUser?.uid
        Log.d("demo", "Creating a new chatroom\nChatroom Name: ${chatroom_name}, User ID: ${fbUserId}")
        if (chatroom_name != null && chatroom_name != "") {
            dbRef.child("chatrooms").child(chatroom_name).setValue(fbUserId)
            Toast.makeText(this.context, "Chatroom Creation Successful", Toast.LENGTH_LONG).show()
            Log.d("demo", "Load chatroom fragment")
        }
    }
}