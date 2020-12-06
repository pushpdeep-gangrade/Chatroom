package com.example.chatroom.ui.ui.chatroom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val list: List<Chat>, private val context: Context)
    : RecyclerView.Adapter<ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChatViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg: Chat = list[position]
        holder.bind(msg, context)


    }
}