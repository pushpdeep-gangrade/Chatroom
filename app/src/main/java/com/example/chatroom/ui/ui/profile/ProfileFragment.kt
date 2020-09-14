package com.example.chatroom.ui.ui.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.data.model.User
import com.example.chatroom.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileFragment : Fragment() {
    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel
    private var auth = FirebaseAuth.getInstance()
    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
            ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
//        val textView: TextView = root.findViewById(R.id.text_gallery)
//        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        dbRef = db.reference

        var fbUser = auth.currentUser
        var firstName_textview = binding.root.findViewById<TextView>(R.id.tv_firstname)
        var lastName_textview = binding.root.findViewById<TextView>(R.id.tv_lastname)
        var gender_textview = binding.root.findViewById<TextView>(R.id.tv_gender)
        var city_textview = binding.root.findViewById<TextView>(R.id.tv_city)
        var profile_imageview = binding.root.findViewById<ImageView>(R.id.profile_image)

        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val userValues = dataSnapshot.getValue<User>()
                firstName_textview.setText(userValues?.firstName)
                lastName_textview.setText(userValues?.lastName)
                gender_textview.setText(userValues?.gender)
                city_textview.setText(userValues?.city)
                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("demo", "loadUser:onCancelled", databaseError.toException())
                // ...
            }
        }

        if (fbUser != null) {
            dbRef.child("users").child(fbUser.uid).addValueEventListener(userListener)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.findViewById<TextView>(R.id.tv_updateProfile).setOnClickListener {
            binding.tvUpdateProfile.findNavController().navigate(R.id.action_nav_profile_to_updateProfile)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}