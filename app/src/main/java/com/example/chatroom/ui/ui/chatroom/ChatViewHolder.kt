package com.example.chatroom.ui.ui.chatroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.example.chatroom.ui.MainActivity
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.MainScope
import kotlinx.serialization.json.Json.Default.context

class ChatViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.chat_item, parent, false)) {
    private var mUserImage : ImageView? = null
    private var mLikeImage : ImageView? = null
    private var mDelImage : ImageView? = null
    private var mTvUser: TextView? = null
    private var mTvLikes: TextView? = null
    private var mTvMsg: TextView? = null
    private var mTvtime: TextView? = null

    init {
        mUserImage = itemView.findViewById(R.id.user_image_chat)
       mLikeImage = itemView.findViewById(R.id.iv_like_msg)
       mDelImage  = itemView.findViewById(R.id.iv_delete_chat)
       mTvUser= itemView.findViewById(R.id.username_chat)
       mTvLikes  = itemView.findViewById(R.id.no_likes)
       mTvMsg = itemView.findViewById(R.id.message_chat)
        mTvtime = itemView.findViewById(R.id.time_chat)

    }
    fun bind(chat: Chat) {
        mTvUser?.text = chat.userfname.plus(" ").plus(chat.userlname)
        mTvLikes?.text = chat.likes.toString()
        mTvMsg?.text = chat.message
        mTvtime?.text = chat.timedate
        Picasso.get().load(chat.userphotourl).resize(250, 250).into(mUserImage);
        if(!MainActivity.globalid.equals(chat.userId))
            mDelImage!!.visibility = View.INVISIBLE

        mDelImage?.setOnClickListener(){
         //     MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child(chat.messageId).setValue("")
        }


        mLikeImage?.setOnClickListener() {
            MainActivity.dbRef.child("chatrooms").child(chatRoomId.toString()).child(chat.messageId)
                .child("likes").setValue(chat.likes + 1)
            mLikeImage!!.setImageResource(R.drawable.heart_icon)
        }
    }


}