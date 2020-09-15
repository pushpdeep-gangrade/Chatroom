package com.example.chatroom.ui.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentChatroomsBinding
import com.example.chatroom.databinding.FragmentUsersBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.ui.chatroom.ChatroomViewModel
import com.example.chatroom.ui.ui.profile.ProfileFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso

class UsersFragment : Fragment() {

    private var _binding : FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var usersViewModel: UsersViewModel
    val users = mutableListOf<String>()
    val usersIds = mutableListOf<String>()

    // access the listView from xml file
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        usersViewModel =
            ViewModelProviders.of(this).get(UsersViewModel::class.java)
        _binding = FragmentUsersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateAdapter()

    }

    fun updateAdapter() {
        MainActivity.dbRef.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val user = postSnapshot.getValue<com.example.chatroom.data.model.User>()!!
                    if (user != null) {
                        usersIds.add(user.userId)
                        users.add(user.firstName.toString() + " " + user.lastName.toString())
                    }
                }
                val arrayAdapter =
                    context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, users) }
                binding.allUsersLiistView.adapter = arrayAdapter

                binding.allUsersLiistView.setOnItemClickListener{ parent, view, position, id ->

                    val bundle = bundleOf("userData" to usersIds[position])
                    view.findNavController().navigate(R.id.action_users_to_nav_profile, bundle)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}