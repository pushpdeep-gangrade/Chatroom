package com.example.chatroom.ui.ui.rider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.data.model.User
import com.example.chatroom.ui.ui.chatroom.Chat
import com.example.chatroom.ui.ui.chatroom.ChatViewHolder

class DriverAdapter(private val list: List<User>, private val view: View?, private val requestId: String)
    : RecyclerView.Adapter<DriverViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DriverViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        val driver: User = list[position]
        holder.bind(driver, view, requestId)
    }
}