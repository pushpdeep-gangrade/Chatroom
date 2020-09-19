package com.example.chatroom.ui.ui.driver

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentWaitForAcceptBinding
import com.example.chatroom.ui.ui.chatroom.chatRoomId

class WaitForAcceptFragment : Fragment() {
    private var _binding : FragmentWaitForAcceptBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
                val bundle = Bundle()
                bundle.putString("chatroomId", chatRoomId.toString())
                view.findNavController().navigate(R.id.action_nav_wait_for_accept_to_chatroom, bundle)
            }
        }
        timer.start()
    }


}