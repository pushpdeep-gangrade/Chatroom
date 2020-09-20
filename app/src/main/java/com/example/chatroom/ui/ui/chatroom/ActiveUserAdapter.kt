package com.example.chatroom.ui.ui.chatroom

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.data.model.User

class ActiveUserAdapter(private val firstnames: List<String>, private val imageURLs: List<String>)
    : RecyclerView.Adapter<ActiveUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ActiveUserViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = firstnames.size


    override fun onBindViewHolder(holder: ActiveUserViewHolder, position: Int) {
        val firstname : String = firstnames[position]
        val imageURL : String = imageURLs[position]
        //Log.d("rock-test", "fullnames: ".plus(firstnames))
        //Log.d("rock-test", "fullname: ".plus(firstname).plus(" | indexed: ").plus(firstnames[position]))
        //Log.d("rock-test", "position: ".plus(position))
        //Log.d("rock-test", "-----------------------------")
        holder.bind(firstname, imageURL)
    }
}
