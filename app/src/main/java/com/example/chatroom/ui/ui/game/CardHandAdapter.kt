package com.example.chatroom.ui.ui.game

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.data.model.User

class CardHandAdapter(private val cards: List<String>, private val gameRequestId: String, private val playerNum: Int)
    : RecyclerView.Adapter<CardHandViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHandViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CardHandViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = cards.size


    override fun onBindViewHolder(holder: CardHandViewHolder, position: Int) {
        val color : String = cards[position][0].toString()
        var value : String = ""
        if (cards[position][0].toString() == "+") {
            value = cards[position]
        }
        else if (cards[position].length > 2) {
            value = "Skip"
        }
        else {
            value = cards[position][1].toString()
        }

        holder.bind(value, color, gameRequestId, playerNum, position)
    }
}
