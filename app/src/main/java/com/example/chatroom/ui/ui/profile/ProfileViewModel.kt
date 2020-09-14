package com.example.chatroom.ui.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatroom.ui.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.util.Assert
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.serialization.json.Json.Default.fromJson

class ProfileViewModel : ViewModel() {
    val db = Firebase.firestore

    private var user = MutableLiveData<User>()

    init{
        fetchUser()
    }

    private fun fetchUser() {
        val docRef = db.collection("Users").document("gzQpGfXvtEmnpFefjiFm")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("demo", "DocumentSnapshot data: ${document.data}")

                } else {
                    Log.d("demo", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("demo", "get failed with ", exception)
            }
//        docRef.get().addOnSuccessListener { documentSnapshot ->
//            documentSnapshot.toObject<User>()
//        }
    }

    fun getUser(): LiveData<User> {
        return user
    }

}

