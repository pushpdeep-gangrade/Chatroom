package com.example.chatroom.ui.ui.driver

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentChatroomBinding
import com.example.chatroom.databinding.FragmentPotentialRiderBinding
import com.example.chatroom.ui.ui.chatroom.chatRoomId

class PotentialRiderFragment : Fragment() {
    private var _binding : FragmentPotentialRiderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        chatRoomId = arguments?.getString("chatroomId")
        _binding = FragmentPotentialRiderBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Button", "Butto Clicked")

        binding.acceptRequestButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_nav_potential_rider_to_nav_wait_for_accept)
        }

        binding.rejectRequestButton.setOnClickListener {
            val bundle1 = Bundle()
            bundle1.putString("chatroomId", chatRoomId.toString())
            view.findNavController().navigate(R.id.action_nav_potential_rider_to_chatroom, bundle1)
        }
    }
}