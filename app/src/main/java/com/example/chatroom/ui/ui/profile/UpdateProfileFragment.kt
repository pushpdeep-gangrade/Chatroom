package com.example.chatroom.ui.ui.profile


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentUpdateProfileBinding
import com.example.chatroom.ui.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


//data class User(val firstname: String, val lastname: String, val gender : String, val city : String, val profileImageUrl : String)

class UpdateProfileFragment : Fragment() {
    var userGender : String? = null
    var profileimageUrl : String? = null
    val REQUEST_IMAGE_CAPTURE = 1
    private var _binding : FragmentUpdateProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateProfileBinding.inflate(inflater, container, false)
        setProfile()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.profileImage.setOnClickListener{
            dispatchTakePictureIntent()
        }

        binding.tvSave.setOnClickListener{
            uploadUserProfile()
            view.findNavController().navigate(R.id.action_updateProfile_to_nav_profile)
        }

        binding.tvCancel.setOnClickListener{
            view.findNavController().navigate(R.id.action_updateProfile_to_nav_profile)
        }



    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            context?.packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            var fbUserId = MainActivity.auth.currentUser?.uid
            if (fbUserId == null) {
                fbUserId = "null"
            }

            val imageBitmap = data?.extras?.get("data") as Bitmap
            var storage = Firebase.storage
            val storageRef = storage.reference
            val userProfileImage = storageRef.child("images").child(fbUserId).child("profilePic.jpg")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()


            var uploadTask = userProfileImage.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { taskSnapshot ->
                Toast.makeText(context, "Upload Success", Toast.LENGTH_LONG).show()
                getDownloadURL(userProfileImage, uploadTask)
                binding.profileImage.setImageBitmap(imageBitmap)
            }
        }
    }

    fun getDownloadURL(ref : StorageReference, uploadTask : UploadTask){
    val urlTask = uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        ref.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result
            profileimageUrl = downloadUri.toString()
            Log.d("demo", downloadUri.toString())
        } else {

        }
    }
    }


    fun uploadUserProfile(){
        var fbUserId = MainActivity.auth.currentUser?.uid
        if (fbUserId == null) {
            fbUserId = "null"
        }
        var fbUserEmail = MainActivity.auth.currentUser?.email
        if (fbUserEmail == null) {
            fbUserEmail = "null"
        }

        val id = binding.radioGroup.checkedRadioButtonId
        when (id) {
            R.id.rb_female ->  userGender = "Female"
            R.id.rb_male -> userGender =  "Male"

        }
        val user = com.example.chatroom.data.model.User()
        user.userId = fbUserId
        user.firstName = binding.tvFirstnameUpdate.text.toString()
        user.lastName = binding.tvLastnameUpdate.text.toString()
        user.gender = userGender.toString()
        user.city = binding.etCity.text.toString()
        user.email = fbUserEmail
        user.imageUrl = profileimageUrl.toString()

        MainActivity.dbRef.child("users").child(user.userId).setValue(user)
    }

    fun setProfile(){
        MainActivity.dbRef.child("users").child(MainActivity.auth.currentUser?.uid.toString()).
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<com.example.chatroom.data.model.User>()
                if (value != null) {
                    binding.tvFirstnameUpdate.text =  Editable.Factory.getInstance().newEditable(value.firstName)
                    binding.tvLastnameUpdate.text =  Editable.Factory.getInstance().newEditable(value.lastName)
                    binding.etCity.text = Editable.Factory.getInstance().newEditable(value.city)

                    when(value.gender){
                        "Male" -> binding.radioGroup.check(R.id.rb_male)
                        "Female" -> binding.radioGroup.check(R.id.rb_female)
                    }

                    profileimageUrl = value.imageUrl
//                    Firebase.storage.reference.child("profile-imgs").child("EoJbgm8dPkcIWHp4Tn5Cd0bRS4i2" + ".png")
//                        .getBytes(Long.MAX_VALUE).addOnSuccessListener {
//                            // Use the bytes to display the image
//                            val bmp =
//                                BitmapFactory.decodeByteArray(it, 0, it.size)
//                            binding.profileImage.setImageBitmap(Bitmap.createScaledBitmap(
//                                    bmp, binding.profileImage.getWidth(),
//                                    binding.profileImage.getHeight(), false
//                                )
//                            )
//                        }.addOnFailureListener {
//                            // Handle any errors
//                        }

                    Picasso.get().load(value.imageUrl).into(binding.profileImage);
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("demo", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

