package com.example.chatroom.ui.ui.rider

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentChatroomsBinding
import com.example.chatroom.databinding.FragmentWaitingOnRideBinding


class WaitingOnRideFragment : Fragment() {
    private var _binding : FragmentWaitingOnRideBinding ? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWaitingOnRideBinding.inflate(inflater, container, false)
        return binding.root
    }



}