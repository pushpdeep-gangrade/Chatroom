package com.example.chatroom.ui.ui.rider

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.R
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentRequestDriverBinding
import com.example.chatroom.ui.ui.chatroom.Chat
import com.squareup.picasso.Picasso

class DriverViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.driver_item, parent, false)){

    private var driverProfilePic : ImageView? = null
    private var driverFullName: TextView? = null
    private var driverLocation: TextView? = null
    private var driverEta: TextView? = null
    private var yesButton: Button? = null
    private var noButton: Button? = null

    init {
        driverProfilePic = itemView.findViewById(R.id.driverItem_profilePic)
        driverFullName = itemView.findViewById(R.id.driverItem_fullName)
        driverLocation  = itemView.findViewById(R.id.driverItem_location)
        driverEta= itemView.findViewById(R.id.driverItem_ETA)
        yesButton  = itemView.findViewById(R.id.driverItem_yesButton)
        noButton = itemView.findViewById(R.id.driverItem_noButton)
    }

    fun bind(driver: User, view: View?) {
        driverFullName?.text = driver.firstName.plus(" ").plus(driver.lastName)
        Picasso.get().load(driver.imageUrl).resize(250, 250).into(driverProfilePic)

        yesButton?.setOnClickListener {
            Log.d("Yes Button", "Clicked")
            view?.findNavController()?.navigate(R.id.action_nav_request_driver_to_nav_waiting_on_ride)
        }

        noButton?.setOnClickListener {
            Log.d("No Button", "Clicked")
        }

    }
}