package com.example.chatroom.ui.ui.driver

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.data.model.RideRequest
import com.example.chatroom.databinding.FragmentWaitForAcceptBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class WaitForAcceptFragment : Fragment() {
    private var _binding : FragmentWaitForAcceptBinding? = null
    private val binding get() = _binding!!
    var requestId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requestId = arguments?.getString("requestId").toString()
        _binding = FragmentWaitForAcceptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val timer = object: CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.waitForAcceptCurrentTime.text = "".plus(millisUntilFinished/1000).plus(" seconds remaining")
            }

            override fun onFinish() {
                MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests").child(requestId)
                    .child("drivers").child(MainActivity.auth.currentUser?.uid.toString()).removeValue()
                val bundle = Bundle()
                bundle.putString("chatroomId", chatRoomId.toString())
                view.findNavController().navigate(R.id.action_nav_wait_for_accept_to_chatroom, bundle)
            }
        }

        timer.start()

        setDriverRequestListener(view, timer)

    }

    fun setDriverRequestListener(view: View, timer: CountDownTimer){

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests").child(requestId)
            .child("drivers").child(MainActivity.auth.currentUser?.uid.toString()).child("status").addValueEventListener(object : ValueEventListener
            {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val status = dataSnapshot.getValue<String>()
                Log.d("Status change", status.toString())
                if(status == "Accepted"){
                    timer.cancel()
                    val bundle = Bundle()
                    bundle.putString("rideId", requestId)
                    view.findNavController().navigate(R.id.action_nav_wait_for_accept_to_nav_on_drive,bundle)
                }
                else if(status == "Rejected"){
                    timer.cancel()
                    showRejectionNotification(view)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("demoo", "cancel")
            }
        })

    }

    fun showRejectionNotification(view: View){
        val builder  =  AlertDialog.Builder(context);
        builder.setTitle("Ride Requests");

        builder.setMessage("This request has been taken by another driver")

        builder.setNegativeButton("Close", DialogInterface.OnClickListener {
                dialog, id -> dialog.cancel()
        })

        builder.setCancelable(false)

        val dialog : AlertDialog = builder.create()
        dialog.show()

        MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child("driverRequests").child(requestId)
            .child("drivers").child(MainActivity.auth.currentUser?.uid.toString()).removeValue()

        val bundle = Bundle()
        bundle.putString("chatroomId", chatRoomId.toString())
        view.findNavController().navigate(R.id.action_nav_wait_for_accept_to_chatroom, bundle)
    }

}