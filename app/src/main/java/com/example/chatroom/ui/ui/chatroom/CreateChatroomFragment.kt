package com.example.chatroom.ui.ui.chatroom

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentCreateChatroomBinding
import com.example.chatroom.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var chatroomNameEdittext = binding.createChatroomTextBox
        var fbUser = auth.currentUser
        var fbUserId = fbUser?.uid

        MainActivity.dbRef.child("users").child(MainActivity.auth.currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("demo", "Firebase event cancelled on getting user data")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageUser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()
                Log.d("Message User", messageUser?.userId.toString() + " whaaaaaaaaaaaaaaaaaaaaaaaaaaat")
            }
        })

        binding.createChatroomAction.setOnClickListener {
            var chatroom_name_text = chatroomNameEdittext.text.toString()
            var allValid = true

            if(chatroom_name_text.equals("")){
                chatroomNameEdittext.error = "Please enter a chatroom name"
                allValid = false
            }
            else if (chatroom_name_text.contains("/")) {
                chatroomNameEdittext.error = "Chatroom name can't contain \'/\' character"
                allValid = false
            }

            if (fbUserId == null) {
                Log.d("demo", "(CreateChatroomFragment.kt) There is a user issue, user id: ${fbUserId}")
                allValid = false
            }

            if (allValid) {
                storeChatroomData(chatroom_name_text, fbUser)
                view.findNavController().navigate(R.id.action_nav_create_chatroom_to_nav_chatrooms)
            }
        }

        binding.cancelCreateChatroomAction.setOnClickListener{
            view.findNavController().navigate(R.id.action_nav_create_chatroom_to_nav_chatrooms)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun storeChatroomData(chatroom_name: String?, fbUser: FirebaseUser?){
        var fbUserId = fbUser?.uid
        Log.d("demo", "Creating a new chatroom\nChatroom Name: ${chatroom_name}, User ID: ${fbUserId}")
        if (chatroom_name != null) {
            val timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofLocalizedDateTime(
                    FormatStyle.SHORT))
            val message = "Welcome to ${chatroom_name}!"
            val templist = mutableMapOf<String, Boolean>()
            val msgKey = MainActivity.dbRef.child("chatrooms").push().key
            val msg = Chat(messageUser?.userId.toString(),
                messageUser?.firstName.toString(),
                messageUser?.lastName.toString(),
                messageUser?.imageUrl.toString(), message, 0, timestamp,msgKey.toString(), templist)
            MainActivity.dbRef.child("chatrooms").child(chatroom_name).child("chatList").child(msgKey.toString()).setValue(msg)
            Toast.makeText(this.context, "Chatroom Creation Successful", Toast.LENGTH_LONG).show()
            Log.d("demo", "Load chatroom fragment")
        }
    }
}