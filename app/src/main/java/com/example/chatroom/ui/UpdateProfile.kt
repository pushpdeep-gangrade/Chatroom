package com.example.chatroom.ui


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentUpdateProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_update_profile.view.*
import java.io.ByteArrayOutputStream
import com.example.chatroom.data.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

//data class User(val firstname: String, val lastname: String, val gender : String, val city : String, val profileImageUrl : String)

class UpdateProfile : Fragment() {
    var userGender : String? = null
    var profileimageUrl : String? = null
    val REQUEST_IMAGE_CAPTURE = 1
    private var _binding : FragmentUpdateProfileBinding? = null
    private val binding get() = _binding!!
    private var auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileImage.setOnClickListener{
            dispatchTakePictureIntent()
        }

        binding.tvSave.setOnClickListener{
            uploadUserProfile()
            findNavController().navigate(R.id.action_updateProfile_to_nav_profile)
        }

        binding.tvCancel.setOnClickListener{
            findNavController().navigate(R.id.action_updateProfile_to_nav_profile)
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
            var fbUserId = auth.currentUser?.uid
            if (fbUserId == null) {
                fbUserId = "null"
            }

            val imageBitmap = data?.extras?.get("data") as Bitmap
            var storage: FirebaseStorage
            storage = Firebase.storage
            val storageRef = storage.reference
            val userProfileImage = storageRef.child("images").child(fbUserId).child("profilePic.jpg")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()


            var uploadTask = userProfileImage.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { taskSnapshot ->
                Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
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
        var fbUserId = auth.currentUser?.uid
        if (fbUserId == null) {
            fbUserId = "null"
        }
        var fbUserEmail = auth.currentUser?.email
        if (fbUserEmail == null) {
            fbUserEmail = "null"
        }

        val id = binding.radioGroup.checkedRadioButtonId
        when (id) {
            R.id.rb_female ->  userGender = "Female"
            R.id.rb_male -> userGender =  "Male"

        }
        val user = User
        user.userId = fbUserId
        user.firstName = binding.tvFirstnameUpdate.text.toString()
        user.lastName = binding.tvLastnameUpdate.text.toString()
        user.gender = userGender.toString()
        user.city = binding.etCity.text.toString()
        user.email = fbUserEmail
        user.imageUrl = profileimageUrl.toString()

        var db = FirebaseDatabase.getInstance()
        var dbRef = db.reference

        dbRef.child("users").child(user.userId).setValue(user)

        //db.collection("Users")
        //    .add(user)
        //    .addOnSuccessListener { documentReference ->
        //        Log.d("demo", "DocumentSnapshot added with ID: ${documentReference.id}")
        //    }
        //    .addOnFailureListener { e ->
        //        Log.w("demo", "Error adding document", e)
        //    }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

