package com.example.chatroom.ui.ui.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.chatroom.R

class ChatroomFragment : Fragment() {

    private lateinit var chatroomViewModel: ChatroomViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatroomViewModel =
            ViewModelProviders.of(this).get(ChatroomViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_chatrooms, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        chatroomViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}