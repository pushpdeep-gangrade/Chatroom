package com.example.chatroom.ui.ui.chatroom

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatroom.databinding.FragmentChatroomBinding
import com.example.chatroom.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

var messageUser : com.example.chatroom.data.model.User? = null
private var listchats = mutableListOf<Chat>()
 var chatRoomId : String? = null

class Chatroom : Fragment() {
    private var _binding : FragmentChatroomBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatRoomId = arguments?.getString("chatroomId")
        _binding = FragmentChatroomBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         chatRoomId  = arguments?.getString("chatroomId")

        initializeList()

        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                   Log.d("demo", "Firebase event cancelled on getting user data")
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    messageUser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()
                }
            })


        binding.sendMessage.setOnClickListener{
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            val message = binding.inputMessage.text.toString()

            if(message!=null){
           val msgKey = MainActivity.dbRef.child("chatrooms").push().key
                val msg = Chat(messageUser?.userId.toString(),
                    messageUser?.firstName.toString(),
                    messageUser?.lastName.toString(),
                    messageUser?.imageUrl.toString(), message, 0, timestamp,msgKey.toString())
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child(msgKey.toString()).setValue(msg)
            binding.inputMessage.setText("").toString()
                updateAdapter()
            }
        }
    }

    fun initializeList(){

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                        listchats.clear()
                for (postSnapshot in dataSnapshot.children) {
                    var value = postSnapshot.getValue<Chat>()
                    if (value != null) {
                        listchats.add(value)
                    }
                }

                updateAdapter()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("demoo", "cancel")
            }
        })
    }
    fun updateAdapter(){
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ChatAdapter(listchats)
        }
    }


}

