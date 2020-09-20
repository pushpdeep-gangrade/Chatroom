package com.example.chatroom.ui.ui.chatroom

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.example.chatroom.data.model.User

import com.squareup.picasso.Picasso

class ActiveUserViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.active_user_item, parent, false)) {

    private var mProfileImageActiveUser : ImageView? = null
    private var mActiveUserName : TextView? = null

    init{
        mProfileImageActiveUser = itemView.findViewById(R.id.active_user_img)
        mActiveUserName = itemView.findViewById(R.id.active_user_name_tv)
    }

    fun bind(firstname: String, imageURL: String) {
        Picasso.get().load(imageURL).into(mProfileImageActiveUser)
        mActiveUserName?.text = firstname

    }


}