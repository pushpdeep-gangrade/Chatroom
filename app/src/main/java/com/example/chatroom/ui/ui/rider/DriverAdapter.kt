package com.example.chatroom.ui.ui.rider

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.data.model.MapUser
import com.example.chatroom.data.model.User
import com.example.chatroom.ui.ui.chatroom.Chat
import com.example.chatroom.ui.ui.chatroom.ChatViewHolder
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.json.Json.Default.context

class DriverAdapter(private val list: List<MapUser>, private val view: View?,
                    private val requestId: String, private val pickupLocationLatLng: LatLng)
    : RecyclerView.Adapter<DriverViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        context = parent.context
        return DriverViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        val driver: MapUser = list[position]
        holder.bind(driver, view, requestId, context, pickupLocationLatLng)
    }
}