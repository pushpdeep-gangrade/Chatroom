package com.example.chatroom.ui.ui.chatroom

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatroom.R
import com.example.chatroom.data.model.User
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

var messageUser : User? = null
private var listchats = mutableListOf<Chat>()
private var listActiveUsers = mutableListOf<String>()
private var listActiveUsersNames = mutableListOf<String>()
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

        Log.d("Active Status", "User is now active")

        initializeList()
        getActiveUsers()

        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                   Log.d("demo", "Firebase event cancelled on getting user data")
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    messageUser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()
                    MainActivity.dbRef.child("activeUsers").child(chatRoomId.toString()).child(messageUser?.userId.toString()).setValue(
                        messageUser)
                }
            })

        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("demo", "Firebase event cancelled on getting user data")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageUser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()
                MainActivity.dbRef.child("activeUsers").child(chatRoomId.toString()).child(messageUser?.userId.toString()).setValue(
                    messageUser)
            }
        })

        binding.chatroomActiveUsers.setOnClickListener {
            /*val activeUserIds = Array(listActiveUsers.size)
            { i ->
                Log.d("Add id $i", listActiveUsers[i].userId.toString())
                listActiveUsers[i].userId.toString() }
            val bundle = bundleOf("activeUserChatroom" to chatRoomId)
            view.findNavController().navigate(R.id.action_chatroom_to_users, bundle)*/

            // setup the alert builder
            var builder  =  AlertDialog.Builder(context);
            builder.setTitle("Active Users");

            builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

            builder.setCancelable(false)

            var userNames = Array<String>(listActiveUsers.size){ i -> listActiveUsersNames[i] }

            builder.setItems(userNames, DialogInterface.OnClickListener(){ dialogInterface: DialogInterface, i: Int ->
                val bundle = bundleOf("userData" to listActiveUsers[i])
                view.findNavController().navigate(R.id.action_chatroom_to_profile, bundle)
            })

            var dialog : AlertDialog = builder.create()
            dialog.show()
        }

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

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("Active Status", "Destroy: User is no longer active")
        Log.d("IDs", "${chatRoomId} ${messageUser?.userId}")
        MainActivity.dbRef.child("activeUsers").child(chatRoomId.toString()).child(messageUser?.userId.toString()).removeValue()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Active Status", "Pause: User is no longer active")
        MainActivity.dbRef.child("activeUsers").child(chatRoomId.toString()).child(messageUser?.userId.toString()).removeValue()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Active Status", "Resume: User is now active")
        MainActivity.dbRef.child("activeUsers").child(chatRoomId.toString()).child(messageUser?.userId.toString()).setValue(
            messageUser)
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

    fun getActiveUsers(){
        MainActivity.dbRef.child("activeUsers").child(chatRoomId.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listActiveUsers.clear()
                listActiveUsersNames.clear()

                for (postSnapshot in dataSnapshot.children) {
                    var value : User? = postSnapshot.getValue<User>()
                    if (value != null) {
                        Log.d("Data change id", value.userId.toString())
                        //Log.d("Data change id", value.toString())
                        var fullName = "${value.firstName} ${value.lastName}"
                        listActiveUsersNames.add(fullName)
                        listActiveUsers.add(value.userId)
                    }
                }

                var activeUsersText = "${listActiveUsers.size} Active User(s)"
                for(user in listActiveUsers){
                    Log.d("Active Users", user)
                }
                for(user in listActiveUsersNames){
                    Log.d("Active Users", user)
                }
                binding.chatroomActiveUsers.setText(activeUsersText)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("demoo", "cancel")
            }
        })
    }


}

