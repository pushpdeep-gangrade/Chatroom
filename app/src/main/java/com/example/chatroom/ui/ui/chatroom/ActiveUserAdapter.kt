package com.example.chatroom.ui.ui.chatroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.data.model.User

class ActiveUserAdapter(private val list: List<User>)
    : RecyclerView.Adapter<ActiveUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ActiveUserViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: ActiveUserViewHolder, position: Int) {
        val user : User = list[position]
        holder.bind(user)
    }
}
