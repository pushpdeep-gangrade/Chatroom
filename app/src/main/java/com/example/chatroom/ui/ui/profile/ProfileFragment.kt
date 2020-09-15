package com.example.chatroom.ui.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentProfileBinding
import com.example.chatroom.ui.MainActivity
import com.example.chatroom.ui.User
import com.example.chatroom.ui.ui.users.UsersFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import java.io.Serializable


class ProfileFragment : Fragment() {
    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel
    var selectedUserId : String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
            ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        selectedUserId = arguments?.getString("userData")

        setProfile()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        binding.tvUpdateProfile.setOnClickListener{
            view.findNavController().navigate(R.id.action_nav_profile_to_updateProfile)
        }
    }

    fun setProfile(){
        var id : String?

        if(selectedUserId == null){
            id = MainActivity.auth.currentUser?.uid
        }
        else{
            id = selectedUserId

            if(!selectedUserId.equals(MainActivity.auth.currentUser?.uid)){
                binding.tvUpdateProfile.visibility = View.GONE
            }
        }

        MainActivity.auth.currentUser?.uid.let {
            if (id != null) {
                MainActivity.dbRef.child("users").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val globaluser = dataSnapshot.getValue<com.example.chatroom.data.model.User>()!!
                        if (globaluser != null) {
                            binding.tvFirstname.text =  globaluser.firstName
                            binding.tvLastname.text =  globaluser.lastName
                            binding.tvGender.text =  globaluser.gender
                            binding.tvCity.text =  globaluser.city
                            Picasso.get().load(globaluser.imageUrl).into(binding.profileImage);
                        }
                        binding.progressBar.visibility = View.INVISIBLE
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("demo", "Failed to read value.", error.toException())
                    }
                })
            }
        }

    }


    companion object{
     //  var globaluser : User = TODO()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

