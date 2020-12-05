package com.example.chatroom.ui.ui.chatroom

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.example.chatroom.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    private var mTranslateButton: TextView? = null

    init {
        mUserImage = itemView.findViewById(R.id.user_image_chat)
       mLikeImage = itemView.findViewById(R.id.iv_like_msg)
       mDelImage  = itemView.findViewById(R.id.iv_delete_chat)
       mTvUser= itemView.findViewById(R.id.username_chat)
       mTvLikes  = itemView.findViewById(R.id.no_likes)
       mTvMsg = itemView.findViewById(R.id.message_chat)
        mTvtime = itemView.findViewById(R.id.time_chat)
        mTranslateButton = itemView.findViewById(R.id.iv_translate_button)

    }
    fun bind(chat: Chat) {
        mTvUser?.text = chat.userfname.plus(" ").plus(chat.userlname)
        mTvLikes?.text = chat.likesMap.size.toString()
        mTvMsg?.text = chat.message
        mTvtime?.text = chat.timedate
        Picasso.get().load(chat.userphotourl).resize(250, 250).into(mUserImage)

        if(!FirebaseAuth.getInstance().currentUser?.uid.equals(chat.userId))
            mDelImage!!.visibility = View.INVISIBLE

        if(chat.likesMap.containsKey(FirebaseAuth.getInstance().currentUser?.uid))
            mLikeImage?.setImageResource(R.drawable.heart_icon)

        mDelImage?.setOnClickListener(){
         var ref = FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomId.toString()).child("chatList").child(chat.messageId)
        ref.removeValue()
        }

        mLikeImage?.setOnClickListener() {
             var ref = FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomId.toString()).child("chatList").child(chat.messageId)
            onLiked(ref)

        }

        mTranslateButton?.setOnClickListener() {
            //This is where the code for
            // 1. Text to Text
            // 2. Text to Talk
            //will go
            Log.d("New Feature","ChatViewHolder.kt file, where Text to Talk and Text to Text translations features are to be added")
        }
    }

    private fun onLiked(postRef: DatabaseReference) {
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val p = currentData.getValue(Chat::class.java)
                    ?: return Transaction.success(currentData)
                var id = FirebaseAuth.getInstance().currentUser?.uid.toString()
                if (p.likesMap.containsKey(FirebaseAuth.getInstance().currentUser?.uid)) {
                   p.likesMap.remove(id)
                    p.likes = p.likesMap.size
                    mLikeImage!!.setImageResource(R.drawable.heart_icon_empty)
                } else {
                p.likesMap[id] = true
                    p.likes = p.likesMap.size
                    mLikeImage!!.setImageResource(R.drawable.heart_icon)
                }
                currentData.value = p
                return Transaction.success(currentData)
            }


            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
            }


        })
    }


}