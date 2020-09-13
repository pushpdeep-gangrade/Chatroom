package com.example.chatroom.ui.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.chatroom.R

class UsersFragment : Fragment() {

    private lateinit var usersViewModel: UsersViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        usersViewModel =
            ViewModelProviders.of(this).get(UsersViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_users, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        usersViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}