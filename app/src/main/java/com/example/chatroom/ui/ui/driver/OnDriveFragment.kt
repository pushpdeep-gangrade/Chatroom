package com.example.chatroom.ui.ui.driver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentOnDriveBinding
import com.example.chatroom.databinding.FragmentPotentialRiderBinding


class OnDriveFragment : Fragment() {
    private var _binding : FragmentOnDriveBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnDriveBinding.inflate(inflater, container, false)

        return binding.root
    }


}