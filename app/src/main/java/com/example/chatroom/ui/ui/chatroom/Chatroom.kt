package com.example.chatroom.ui.ui.chatroom

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatroom.R
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentChatroomBinding
import com.example.chatroom.ui.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

var messageUser : User? = null
var messageUserId: String = ""

private var listchats = mutableListOf<Chat>()
private var listActiveUsers = mutableListOf<String>()
private var listActiveUsersNames = mutableListOf<String>()
private var listActiveUserImageURLs = mutableListOf<String>()

private var listRideRequests = mutableListOf<String>()
private var listRideRequestsObjects = mutableListOf<RideRequest>()
private var listRideRequestNames= mutableListOf<String>()

private var activeUsers  = mutableListOf<User>()
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
        setRideRequestListener(view)

        MainActivity.dbRef.child("users").child(MainActivity.auth.currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                   Log.d("demo", "Firebase event cancelled on getting user data")
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    messageUser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()
                    Log.d("Message User", messageUser?.userId.toString() + " whaaaaaaaaaaaaaaaaaaaaaaaaaaat")
                    messageUserId = messageUser?.userId.toString()
                    MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers").child(messageUser?.userId.toString()).setValue(
                        messageUser)
                }
            })

        getActiveUsers()

//        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("demo", "Firebase event cancelled on getting user data")
//            }
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                messageUser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()
//                MainActivity.dbRef.child("activeUsers").child(chatRoomId.toString()).child(messageUser?.userId.toString()).setValue(
//                    messageUser)
//            }
//        })

        binding.chatroomActiveUsers.setOnClickListener {
            var builder  =  AlertDialog.Builder(context);
            builder.setTitle("Active Users");

            builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

            builder.setCancelable(false)

            //var userNames = Array<String>(listActiveUsers.size){ i -> listActiveUsersNames[i] }
            var userNames = listActiveUsersNames.toTypedArray()

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
            //val templist = mutableListOf<String>()
            val templist = mutableMapOf<String, Boolean>()

            if(!message.isEmpty()){
           val msgKey = MainActivity.dbRef.child("chatrooms").push().key
                val msg = Chat(messageUser?.userId.toString(),
                    messageUser?.firstName.toString(),
                    messageUser?.lastName.toString(),
                    messageUser?.imageUrl.toString(), message, 0, timestamp,msgKey.toString(), templist)
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("chatList").child(msgKey.toString()).setValue(msg)
            binding.inputMessage.setText("").toString()
                updateAdapter()
            }
        }

        binding.requestRideButton.setOnClickListener {
            val bundle2 = Bundle()
            bundle2.putString("chatroomId", chatRoomId.toString())
            //This is for rider view (testing)
            view.findNavController().navigate(R.id.action_chatroom_to_nav_request_ride, bundle2)


        }

        // this would be deleted. Just for testing layouts at the moment -------------------------------------------------------------------
        binding.requestRideButton.setOnLongClickListener {
            val bundle3 = Bundle()
            bundle3.putString("chatroomId", chatRoomId.toString())
            //This is for driver view (testing)
            view.findNavController().navigate(R.id.action_chatroom_to_nav_potential_rider, bundle3)
            true
        }
        //----------------------------------------------------------------------------------------------------------------------------------
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("Active Status", "Destroy: User is no longer active")
        Log.d("IDs", "${chatRoomId} ${messageUser?.userId} $messageUserId")
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers").child(messageUserId.toString()).removeValue()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Active Status", "Pause: User is no longer active")
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers").child(messageUserId.toString()).removeValue()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Active Status", "Resume: User is now active")
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers").child(messageUserId.toString()).setValue(
            messageUser)
    }

    fun initializeList(){

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("chatList").addValueEventListener(object : ValueEventListener {
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
                Log.d("demo", "cancel")
            }
        })
    }

    fun updateAdapter(){
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ChatAdapter(listchats)
        }
    }

    override fun onStop() {
        super.onStop()
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers").child(messageUserId.toString()).removeValue()
    }

    fun getActiveUsers(){
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("listActiveUsers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listActiveUsers.clear()
                listActiveUsersNames.clear()
                listActiveUserImageURLs.clear()

                for (postSnapshot in dataSnapshot.children) {
                    var u : User? = postSnapshot.getValue<User>()
                    if (u != null) {
//                        Log.d("Data change id", value.userId.toString())
//                        //Log.d("Data change id", value.toString())
//                        var fullName = "${value.firstName} ${value.lastName}"
//                        listActiveUsersNames.add(fullName)
                        listActiveUsers.add(u.userId)
                        listActiveUsersNames.add(u.firstName)
                        listActiveUserImageURLs.add(u.imageUrl)
                    }
                }
                updateActiveUsers()
                //for(ac in activeUsers){
                //    Log.d("check active user " , "${ac.firstName}")
                //}

//                var activeUsersText = "${listActiveUsers.size} Active User(s)"
//                for(user in listActiveUsers){
//                    Log.d("Active Users", user)
//                }
//                for(user in listActiveUsersNames){
//                    Log.d("Active Users", user)
//                }
//                binding.chatroomActiveUsers.setText(activeUsersText)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("demoo", "cancel")
            }
        })
    }

    fun updateActiveUsers(){
        binding.activeUserRcylerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL ,false)
        binding.activeUserRcylerView.adapter = ActiveUserAdapter(listActiveUsersNames, listActiveUserImageURLs)
    }

    fun setRideRequestListener(view: View){
        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("rideRequests").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("Ride Request", "Ride Request Updated")

                listRideRequests.clear()
                listRideRequestNames.clear()
                listRideRequestsObjects.clear()

                for (postSnapshot in dataSnapshot.children) {
                    var rr : RideRequest? = postSnapshot.getValue<RideRequest>()
                    if (rr != null) {
                        listRideRequests.add(rr.requestId)
                        var name = rr.riderInfo.firstName.plus(" ").plus(rr.riderInfo.lastName)
                            .plus(": ").plus(rr.pickupLocation.name).plus(" to ")
                            .plus(rr.dropoffLocation.name)
                        listRideRequestNames.add(name)
                        listRideRequestsObjects.add(rr)

                    }
                }

                var active = false

                for(id in listActiveUsers){
                    if(id == messageUserId){
                        active = true
                    }
                }

                if(active){
                    showNotificationDialog(view)
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("demoo", "cancel")
            }
        })
    }

    fun showNotificationDialog(view: View){
        if(context!=null){
        var builder  =  AlertDialog.Builder(context);
        builder.setTitle("Ride Requests");

        builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                dialog, id -> dialog.cancel()
        })

        builder.setCancelable(false)

        var requestNames = listRideRequestNames.toTypedArray()

        builder.setItems(requestNames, DialogInterface.OnClickListener(){ dialogInterface: DialogInterface, i: Int ->
            //val bundle = bundleOf("userData" to listRideRequests[i])
            val bundle = bundleOf("chatroomId" to chatRoomId, "requestId" to listRideRequests[i])
            view.findNavController().navigate(R.id.action_chatroom_to_nav_potential_rider, bundle)
        })

        var dialog : AlertDialog = builder.create()
        dialog.show()
        }
    }


}

