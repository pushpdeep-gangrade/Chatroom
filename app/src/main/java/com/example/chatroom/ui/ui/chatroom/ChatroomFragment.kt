package com.example.chatroom.ui.ui.chatroom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentChatroomsBinding
import com.example.chatroom.databinding.FragmentProfileBinding
import com.example.chatroom.ui.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ChatroomFragment : Fragment() {
    private var _binding : FragmentChatroomsBinding ? = null
    private val binding get() = _binding!!
    private lateinit var chatroomViewModel: ChatroomViewModel
    val chatrooms = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatroomViewModel =
            ViewModelProviders.of(this).get(ChatroomViewModel::class.java)
        _binding = FragmentChatroomsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateAdapter()

        binding.listView.setOnItemClickListener(){ adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            val chatroomId = adapterView.getItemAtPosition(i)
            val bundle = Bundle()
            bundle.putString("chatroomId", chatroomId.toString())
            view.findNavController().navigate(R.id.action_nav_chatrooms_to_chatroom, bundle)
        }



    }

    fun updateAdapter() {
        MainActivity.dbRef.child("chatrooms").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatrooms.removeAll(chatrooms)
                for (postSnapshot in dataSnapshot.children) {
                   chatrooms.add(postSnapshot.key.toString())
                }
                val arrayAdapter =
                    context?.let { ArrayAdapter<String>(it,android.R.layout.simple_list_item_1,chatrooms ) }
                binding.listView.adapter = arrayAdapter
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

}