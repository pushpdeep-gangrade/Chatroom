package com.example.chatroom.ui.ui.rider

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentRequestRideBinding

class RequestRideFragment : Fragment() {
    private var _binding : FragmentRequestRideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRequestRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Button", "Butto Clicked")

        binding.submitRequestButton.setOnClickListener {
            //Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
            view.findNavController().navigate(R.id.action_nav_request_ride_to_nav_request_driver)//, bundle)
        }
    }
}